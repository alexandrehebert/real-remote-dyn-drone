package fr.upmc.r2d2.mains;

import fr.upmc.dtgui.annotations.BooleanActuatorData;
import fr.upmc.dtgui.annotations.BooleanSensorData;
import fr.upmc.dtgui.annotations.IntegerActuatorData;
import fr.upmc.dtgui.annotations.IntegerSensorData;
import fr.upmc.dtgui.annotations.RealActuatorData;
import fr.upmc.dtgui.annotations.RealSensorData;
import fr.upmc.dtgui.annotations.WithActuators;
import fr.upmc.dtgui.annotations.WithSensors;
import fr.upmc.r2d2.tools.AnnotationPrinter;
import fr.upmc.r2d2.tools.AssistantLoader;
import fr.upmc.r2d2.tools.Utils;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainJavassist_2 {

    private static final WalkAnnotations WALKER = new WalkAnnotations();
    private static final AssistantLoader LOADER = new AssistantLoader();
    
    private static GuiTranslator guiTranslator = new GuiTranslator();
    private static RobotTranslator robotTranslator = new RobotTranslator();
    
    public static void main(String[] args) throws Throwable {
        LOADER.addTranslator(robotTranslator, "@fr.upmc.dtgui.annotations.WithSensors");
//        LOADER.addTranslator(new RobotWithActuatorsTranslator(), "@fr.upmc.dtgui.annotations.WithActuators");
        // LOADER.addTranslator(new RobotTranslator(), "fr.upmc.dtgui.robot.Robot");
        LOADER.addTranslator(guiTranslator, "fr.upmc.r2d2.boards.DynGUI");
        
        LOADER.run("fr.upmc.r2d2.mains.MainTests");
        //LOADER.run("fr.upmc.r2d2.tests.MainTests");
    }

    private static class GuiTranslator implements AssistantLoader.ISimpleTranslator  {
        public static final String TBOARD_EXT = "TeleoperationBoard";
        private StringBuffer boards = new StringBuffer();
        
        public void addBoard(String robotType) {
            boards.append("boards.put("+robotType+".class, new " + robotType + TBOARD_EXT + "(this, sizeX - 50));");
        }
        
        /**
         * Méthode appelée pour chaque classe annotée par WithSensors
         * 
         * @param cp
         * @param string
         * @param cc CtClass de Robot
         * @throws Exception 
         */
        @Override
        public void onLoad(ClassPool cp, String string, final CtClass cc) throws Exception {
            Utils.block(cc.getSimpleName(), new Utils.Block() {

                @Override
                public void run() throws Exception {
                    cc.getConstructors()[0].insertAfter(boards.toString());
                }
            });

        }        
    }

    /**
     * Parcours des classes annotées au minimum par WithSensors,
     * donc, typiquement : les robots
     * RobotTranslator ne sera prévenu que du chargement de ces classes,
     * et d'aucunes autres
     */
    private static class RobotTranslator implements AssistantLoader.ISimpleTranslator {

        /**
         * Méthode appelée pour chaque classe annotée par WithSensors
         * 
         * @param cp
         * @param string
         * @param cc CtClass de Robot
         * @throws Exception 
         */
        @Override
        public void onLoad(ClassPool cp, String string, final CtClass cc) throws Exception {

            Utils.block(cc.getSimpleName(), new Utils.Block() {

                @Override
                public void run() throws Exception {
                    final Makeable maker = (Makeable) Proxy.newProxyInstance(
                            RobotMaker.class.getClassLoader(),
                            new Class[]{Makeable.class},
                            new RobotMaker(cc));

                    WALKER.walk(cc, new WalkAnnotations.IAnnotationWalker() {

                        @Override
                        public void walk(CtMethod m, Annotation a) throws Exception {
                            maker.processAnnotation(m, m.getName()/*.substring(3)*/, a);
                        }
                    });

                    maker.make();
                }
            });

        }
    }

    /**
     * Utilitaire de parcours simplifié d'annotations pour une classe donnée
     */
    private static class WalkAnnotations {

        /**
         * Interface à implémenter si l'on veut recevoir pour chaque annotation
         * trouvée sur une méthode donnée, un évènement
         * Il s'agit donc du handler d'annotations
         */
        public interface IAnnotationWalker {

            public void walk(CtMethod m, Annotation a) throws Exception;
        }

        /**
         * Parcours des annotations de l'ensemble des méthodes d'une classe donnée
         * 
         * @param c
         * @param walker handler d'annotations
         */
        public void walk(CtClass c, IAnnotationWalker walker) {
            for (CtMethod method : c.getMethods()) {
                try {
                    for (Object o : method.getAnnotations()) {
                        try {
                            walker.walk(method, (Annotation) o);
                        } catch (Exception e) {
                            Utils.print(e);
                        }
                    }
                } catch (Exception e) {
                    Utils.print(e);
                }
            }
        }
    }

    /**
     * Nous permet de "génériser" le parcours des annotations en dispatchant automatiquement
     * le traitement de telle ou telle annotation vers la méthode adéquate
     */
    public static interface Makeable extends InvocationHandler {

        /**
         * Finalisation de la fabrication de l'objet makeable
         * 
         * @throws Exception 
         */
        public void make() throws Exception;

        /**
         * Traitement générique de l'annotation (par reflexion)
         * Si l'annotation est d'un type T qui nous permet de résoudre la recherche
         * d'une méthode de signature :
         *  processAnnotation(CtMethod m, String name, T sensor)
         * alors, on délègue le traitement de l'annotation à cette dernière
         * 
         * @param m
         * @param name
         * @param sensor
         * @throws Exception 
         */
        public void processAnnotation(CtMethod m, String name, Annotation sensor) throws Exception;
    }

    /**
     * La Frabrique de robot est la classe qui est chargée d'aggréger l'ensemble des
     * traitements qu'il nous sera possible pour telle ou telle annotation trouvée
     * qu'elle soit de type Sensor ou Actuator
     */
    public static class RobotMaker implements Makeable {

        private StringBuffer tmpQueue = new StringBuffer();
        private Map<CtMethod, Annotation> sensors = new HashMap(), 
                actuators = new HashMap();
        private CtClass robot;
        private CtClass dataSender;
        private CtClass dataReceptor;
        private ClassPool pool;
        public static final String SENDER_EXT = "SensorDataSender";
        public static final String RECEPTOR_EXT = "ActuatorDataReceptor";

        public RobotMaker(CtClass robot) {
            this.robot = robot;
            this.pool = robot.getClassPool();
            pool.importPackage("java.util.concurrent");
            pool.importPackage("java.lang.Thread");
            System.out.println("> process annotations :");
        }

        private void processAnnotation(String groupName, String name, String type) {
            tmpQueue.append(("dataQueue.add(new fr.upmc.r2d2.tools.MessageData(\"" + groupName + "\",\"" + name + "\", new " + type + "(robot." + name + "())));"));
        }

        /*******************************************************
         * SENSORS
         *******************************************************/
        
        public void processAnnotation(CtMethod m, String name, RealSensorData sensor) {
            processAnnotation(sensor.groupName(), name, "Double");
        }

        public void processAnnotation(CtMethod m, String name, IntegerSensorData sensor) {
            processAnnotation(sensor.groupName(), name, "Integer");
        }
        
        public void processAnnotation(CtMethod m, String name, BooleanSensorData sensor) {
            processAnnotation(sensor.groupName(), name, "Boolean");
        }
        
        /*******************************************************
         * ACTUATORS
         *******************************************************/
        
        public void processAnnotation(CtMethod m, String name, RealActuatorData sensor) {
            /* Pour le TBoard */
        }
        
        public void processAnnotation(CtMethod m, String name, IntegerActuatorData sensor) {
            /* Pour le TBoard */
        }
        
        public void processAnnotation(CtMethod m, String name, BooleanActuatorData sensor) {
            /* Pour le TBoard */
        }    
        
       /**
         * On cherche, pour une annotation donnée, la méthode qui la traite
         * 
         * @param m
         * @param name
         * @param a
         * @throws Exception 
         */
        @Override
        public void processAnnotation(CtMethod m, String name, Annotation a) throws Exception {
            Method process;

            try {
                process = getClass().getMethod("processAnnotation", CtMethod.class, String.class, a.annotationType());
            } catch (Throwable ex) {
                return;
            } // si on ne trouve pas la méthode tant pis, pas besoin de faire remonter d'exception

            AnnotationPrinter ap = new AnnotationPrinter(a);
            System.out.println("\t" + ap + " " + name.substring(3));

            switch (ap.getType()) {
                case ACTUATOR:
                    actuators.put(m, a);
                    break;
                case SENSOR:
                    sensors.put(m, a);
                    break;
            }

            process.invoke(this, m, name, a);
        }     
        
        /*******************************************************
         * CONSTRUCTIONS DES ACTUATORS ET SENSORS
         *******************************************************/

        /**
         * On génére le code à partir des senseurs et des actuateurs trouvés
         * 
         * @throws Exception 
         */
        @Override
        public void make() throws Exception {
            robot.setInterfaces(new CtClass[]{LOADER.getCtClass(pool, "fr.upmc.dtgui.robot.InstrumentedRobot")});
            
            if (hasSensors()) {
                System.out.println("\tclass " + robot.getSimpleName() + SENDER_EXT);
                makeSensors();
            }
            
            if (hasActuators()) {
                System.out.println("\tclass " + robot.getSimpleName() + RECEPTOR_EXT);
                makeActuators();
            }
            
            robot.writeFile();
            
            /* créer le tboard associé */
        }

        public void makeSensors() throws Exception {
            /* Création de la SensorQueue du robot */
            
            dataSender = makeDataQueue(SENDER_EXT, sensors.size());
            dataSender.addField(CtField.make("private int sleep = 100;", dataSender));

            // construction du code des méthodes et constructeurs
            String run = Utils.readSnippet(SENDER_EXT + ".run").replaceFirst("#TMPQUEUE#", tmpQueue.toString());

            // ajout des méthodes
            dataSender.addMethod(CtMethod.make(Utils.readSnippet(SENDER_EXT + ".start"), dataSender));
            dataSender.addMethod(CtMethod.make(run, dataSender));

            dataSender.writeFile();

            /* 
             * Création d'une instance de SensorSender pour le robot, 
             * et ajout du code de lancement du thread dans le constructeur du robot
             */
            robot.addField(CtField.make("private " + robot.getName() + SENDER_EXT + " sds;", robot));
            robot.getDeclaredMethod("start").insertBefore("sds.start();");
            robot.addMethod(CtMethod.make(Utils.readSnippet("Robot.getSensorDataQueue"), robot));
            robot.getConstructors()[0].insertAfter("sds = new " + robot.getName() + SENDER_EXT + "(this);");
        }

        public void makeActuators() throws Exception {
            dataReceptor = makeDataQueue(RECEPTOR_EXT, 1);

            // ajout des méthodes
            dataSender.addMethod(CtMethod.make(Utils.readSnippet(RECEPTOR_EXT + ".start"), dataSender));
            dataSender.addMethod(CtMethod.make(Utils.readSnippet(RECEPTOR_EXT + ".run"), dataSender));         
            
            dataReceptor.writeFile();

            /* 
             * Création d'une instance de ActuatorReceptor pour le robot, 
             * et ajout du code de lancement du thread dans le constructeur du robot
             */
            robot.addField(CtField.make("private " + robot.getName() + RECEPTOR_EXT + " adr;", robot));
            robot.getDeclaredMethod("start").insertBefore("adr.start();");
            robot.addMethod(CtMethod.make(Utils.readSnippet("Robot.getActuatorDataQueue"), robot));
            robot.getConstructors()[0].insertAfter("adr = new " + robot.getName() + RECEPTOR_EXT + "(this);");
        }
        
        /**
         * Création de la classe TeleoperationBoard associé à ce type de robot
         */
        public void makeBoard() {
            guiTranslator.addBoard(robot.getName());
            
            
        }
        
        /*******************************************************
         * OUTILS
         *******************************************************/
        
        public boolean hasSensors() {
            return robot.hasAnnotation(WithSensors.class);
        }

        public boolean hasActuators() {
            return robot.hasAnnotation(WithActuators.class);
        }        

        /**
         * Créer une nouvelle classe de DataQueue
         * @param ext
         * @param size
         * @return
         * @throws Exception 
         */
        private CtClass makeDataQueue(String ext, int size) throws Exception {

            CtClass dataQueue = pool.makeClass(
                    // getName doit retourner le nom complet de la classe + package
                    robot.getName() + ext,
                    LOADER.getCtClass(pool, "java.lang.Thread"));

            dataQueue.addField(CtField.make("public final BlockingQueue dataQueue = new ArrayBlockingQueue(" + size + ");", dataQueue));
            dataQueue.addField(CtField.make("private " + robot.getName() + " robot;", dataQueue));

            CtConstructor cons = new CtConstructor(new CtClass[]{robot}, dataQueue);
            cons.callsSuper();
            cons.setBody("this.robot = robot;");
            dataQueue.addConstructor(cons);

            return dataQueue;

        }
        
        /*******************************************************
         * Debug
         *******************************************************/        

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("make")) {
                System.out.println("> make " + robot.getSimpleName() + " data queues :");
                System.out.println("\tpackage " + robot.getPackageName());
            }
            return method.invoke(this, args);
        }
    }

    private static class WorldTranslator implements AssistantLoader.ISimpleTranslator {

        @Override
        public void onLoad(ClassPool cp, String string, CtClass c) throws Exception {

            Utils.block("<<< World >>>", new Utils.Block() {

                @Override
                public void run() throws Exception {
                }
            });
        }
    }
}
