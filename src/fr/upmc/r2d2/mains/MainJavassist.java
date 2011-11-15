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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import static fr.upmc.r2d2.tools.Utils.Block;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainJavassist {

    private static final WalkAnnotations WALKER = new WalkAnnotations();
    private static final AssistantLoader LOADER = new AssistantLoader();
    
    private static GuiTranslator guiTranslator = new GuiTranslator();
    private static RobotTranslator robotTranslator = new RobotTranslator();
    
    public static void main(String[] args) throws Throwable {
        LOADER.addTranslator(robotTranslator, "@fr.upmc.dtgui.annotations.WithSensors");
        LOADER.addTranslator(guiTranslator, "fr.upmc.r2d2.boards.DynGUI");
        String main = (args.length > 0) ? args[0] : Mains.DEFAULT;
        LOADER.run(main);
    }
    
    /**
     * Translator qui se charge de l'ensemble de la génération de code associée au DynGUI
     * On ajoute l'instanciation des TBoards et leur stockage dans une table de hachage
     * On pourra ainsi les récupérer en fonction du robot détécté
     */
    private static class GuiTranslator implements AssistantLoader.ISimpleTranslator  {
        
        public static final String TBOARD_EXT = "TeleoperationBoard";
        private StringBuffer boards = new StringBuffer();
        private String debug = "";
        
        /**
         * Ajout d'un TBoard à la liste globale des TBoards
         * Le TBoard est identifié par le nom complet de la classe du robot
         * correspondant
         * 
         * @param robotType canonicalName du robot pour lequel on veut créer un TBoard
         */
        public void addBoard(String robotType) {
            boards.append("boards.put(")
                    .append(robotType)
                    .append(".class, new ")
                    .append(robotType)
                    .append(TBOARD_EXT + "(this, sizeX - 50));\n");
            Block.print("> create board :\n\t" + robotType + TBOARD_EXT);
            debug += "\tboard " + robotType + "\n";
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
                    
                    Block.print("> process boards :\n" + debug + "> inserting code in DynGUI");
                    Utils.log(boards.toString());
                    cc.getConstructors()[0].insertAfter(boards.toString());
                    cc.writeFile();
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

                    /**
                     * On parcours la liste des annotations de chaque méthode
                     */
                    WALKER.walk(cc, new WalkAnnotations.IAnnotationWalker() {
                        public void walk(CtMethod m, Annotation a) throws Exception {
                            /**
                             * On cherche à résoudre l'annotation dans la liste de nos
                             * méthodes de traitement
                             */
                            maker.processAnnotation(m, m.getName()/*.substring(3)*/, a);
                        }
                    });

                    /**
                     * On fabrique le robot final
                     */
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
     * traitements qu'il nous faudra effectuer pour telle ou telle annotation trouvée
     * qu'elle soit de type Sensor ou Actuator
     */
    public static class RobotMaker implements Makeable {

        private StringBuffer tmpQueue = new StringBuffer();
        private Map<CtMethod, Annotation> sensors = new HashMap(), 
                actuators = new HashMap();
        private Map<String, StringBuffer> groupPanels = new HashMap();
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
            Block.print("> process annotations :");
        }

        /**
         * Ajoute le bout de code généré au tampon de code qui sera ajouté
         * dans la classe XXXSensorDataSender
         * 
         * @param groupName
         * @param name
         * @param type 
         */
        private void processAnnotation(String groupName, String name, String type) {
            tmpQueue.append(("dataQueue.add(new MessageData(\"" + groupName + "\",\"" + name + "\", new " + type + "(robot." + name + "())));"));
        }
        
        /**
         * Ajoute le groupe trouvé à la liste des groupes de panneaux qui seront
         * ajouté dans le TBoard du robot
         * 
         * @param groupName
         * @param component 
         */
        private void processGroup(String groupName, String component) {
            if (!groupPanels.containsKey(groupName)) {
                groupPanels.put(groupName, new StringBuffer("GroupPanel "+ groupName + " = new GroupPanel(\""+ groupName +"\");"
                        + "panels.add(" + groupName + ");"));
                Block.print("\tgroup " + groupName);
            }
            
            groupPanels.get(groupName)
                    .append("addComponent(")
                    .append(groupName)
                    .append(", new ")
                    .append(component)
                    .append(");");
        }

        /*******************************************************
         * SENSORS
         *******************************************************/
        
        public void processAnnotation(CtMethod m, String name, RealSensorData sensor) {
            processAnnotation(sensor.groupName(), name, "Double");
            /* String groupName, String methodName, double minRate, double maxRate, String unit, double minRange, double maxRange, VariationType variation */
            processGroup(sensor.groupName(), 
                    String.format(Locale.US, "RealDisplayPanel(\"%s\", \"%s\", %f, %f, \"%s\", %f, %f, VariationType.%s)", 
                    sensor.groupName(),
                    name, 
                    sensor.minReadingRate(),
                    sensor.maxReadingRate(),
                    sensor.unit().name(),
                    sensor.dataRange().inf(),
                    sensor.dataRange().sup(),
                    sensor.variation().toString()
            ));
        }

        public void processAnnotation(CtMethod m, String name, IntegerSensorData sensor) {
            processAnnotation(sensor.groupName(), name, "Integer");
            processGroup(sensor.groupName(), 
                    String.format(Locale.US, "IntegerDisplayPanel(\"%s\", \"%s\", %f, %f, \"%s\", %d, %d, VariationType.%s)", 
                    sensor.groupName(),
                    name, 
                    sensor.minReadingRate(),
                    sensor.maxReadingRate(),
                    sensor.unit().name(),
                    sensor.dataRange().inf(),
                    sensor.dataRange().sup(),
                    sensor.variation().toString()
            ));
        }
        
        public void processAnnotation(CtMethod m, String name, BooleanSensorData sensor) {
            processAnnotation(sensor.groupName(), name, "Boolean");
            processGroup(sensor.groupName(), 
                    String.format(Locale.US, "BooleanDisplayPanel(\"%s\", \"%s\", %f, %f)", 
                    sensor.groupName(),
                    name, 
                    sensor.minReadingRate(),
                    sensor.maxReadingRate()
            ));
        }
        
        /*******************************************************
         * ACTUATORS
         *******************************************************/
        
        public void processAnnotation(CtMethod m, String name, RealActuatorData sensor) {
            /* String groupName, String methodName, double minWritingRate, double maxWritingRate, String unit, double minRange, double maxRange */
            processGroup(sensor.groupName(), 
                    String.format(Locale.US, "RealControllerPanel(\"%s\", \"%s\", %f, %f, \"%s\", %f, %f)", 
                    sensor.groupName(),
                    name, 
                    sensor.minWritingRate(),
                    sensor.maxWritingRate(),
                    sensor.unit().name(),
                    sensor.dataRange().inf(),
                    sensor.dataRange().sup()
            ));
        }
        
        public void processAnnotation(CtMethod m, String name, IntegerActuatorData sensor) {
            processGroup(sensor.groupName(), 
                    String.format(Locale.US, "RealControllerPanel(\"%s\", \"%s\", %f, %f, \"%s\", %d, %d)", 
                    sensor.groupName(),
                    name, 
                    sensor.minWritingRate(),
                    sensor.maxWritingRate(),
                    sensor.unit().name(),
                    sensor.dataRange().inf(),
                    sensor.dataRange().sup()
            ));
        }
        
        public void processAnnotation(CtMethod m, String name, BooleanActuatorData sensor) {
            processGroup(sensor.groupName(), 
                    String.format(Locale.US, "RealControllerPanel(\"%s\", \"%s\", %f, %f)", 
                    sensor.groupName(),
                    name, 
                    sensor.minWritingRate(),
                    sensor.maxWritingRate()
            ));
        }    
        
       /**
         * On cherche, pour une annotation donnée, la méthode qui la traite
         * Exemple : 
         *  Ainsi, pour l'annotation RealSensorData, par reflexion on va chercher la 
         *  méthode de signature "public void processAnnotation(CtMethod m, String name, RealSensorData sensor)"
         *  et lui faire executer le job
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
            } catch (NoSuchMethodException | SecurityException ex) {
                Utils.log("Il n'existe pas de traitement associé à "+ a.annotationType().getName() +" : "+ ex.getMessage());
                return;
            } // si on ne trouve pas la méthode tant pis, pas besoin de faire remonter d'exception

            AnnotationPrinter ap = new AnnotationPrinter(a);
            Block.print("\t" + ap + " " + name.substring(3));

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
            
            //if (hasSensors()) {
                Block.print("\tclass " + robot.getSimpleName() + SENDER_EXT);
                makeSensors();
            //}
            
            //if (hasActuators()) { on doit créé une queue dans tous les cas, qu'elle soit vide ou non
                Block.print("\tclass " + robot.getSimpleName() + RECEPTOR_EXT);
                makeActuators();
            //}
            
            robot.writeFile();
            
            /* créer le tboard associé */
            makeBoard();
        }

        /**
         * On a recensé l'ensemble des sensors, on peut générer le code en conséquence
         * 
         * @throws Exception 
         */
        public void makeSensors() throws Exception {
            /* Création de la SensorQueue du robot */
            
            dataSender = makeDataQueue(SENDER_EXT, sensors.size());
            dataSender.getClassPool().importPackage("fr.upmc.r2d2.robots.MessageData");
            
            dataSender.addField(CtField.make("private int sleep = 100;", dataSender));

            // construction du code des méthodes et constructeurs
            String run = Utils.readSnippet(SENDER_EXT + ".run").replaceFirst("#TMPQUEUE#", tmpQueue.toString());
            
            Utils.log(run);

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
            robot.getConstructors()[0].insertAfter("sds = new " + robot.getName() + SENDER_EXT + "($0);");
        }

        /**
         * On a recensé l'ensemble des actuators, on peut générer le code en conséquence
         * 
         * @throws Exception 
         */
        public void makeActuators() throws Exception {
            dataReceptor = makeDataQueue(RECEPTOR_EXT, 1);
            
            dataReceptor.getClassPool().importPackage("fr.upmc.r2d2.robots.ActuatorCommand");
            dataReceptor.getClassPool().importPackage("fr.upmc.r2d2.robots.MessageData");

            // ajout des méthodes
            dataReceptor.addMethod(CtMethod.make(Utils.readSnippet(RECEPTOR_EXT + ".start"), dataReceptor));
            dataReceptor.addMethod(CtMethod.make(Utils.readSnippet(RECEPTOR_EXT + ".run"), dataReceptor));         
            
            dataReceptor.writeFile();

            /* 
             * Création d'une instance de ActuatorReceptor pour le robot, 
             * et ajout du code de lancement du thread dans le constructeur du robot
             */
            robot.addField(CtField.make("private " + robot.getName() + RECEPTOR_EXT + " adr;", robot));
            robot.getDeclaredMethod("start").insertBefore("adr.start();");
            robot.addMethod(CtMethod.make(Utils.readSnippet("Robot.getActuatorDataQueue"), robot));
            // robot.getDeclaredMethod("getActuatorDataQueue").setModifiers(Modifier.ABSTRACT); 
            robot.getConstructors()[0].insertAfter("adr = new " + robot.getName() + RECEPTOR_EXT + "($0);");
        }
        
        /**
         * Création de la classe TeleoperationBoard associé à ce type de robot
         */
        public void makeBoard() throws Exception {
            guiTranslator.addBoard(robot.getName());
            
            CtClass board = pool.makeClass(robot.getName() + GuiTranslator.TBOARD_EXT, LOADER.getCtClass(pool, "fr.upmc.r2d2.boards.Abstract" + GuiTranslator.TBOARD_EXT));
            board.getClassPool().importPackage("fr.upmc.r2d2.boards.GroupPanel");
            board.getClassPool().importPackage("fr.upmc.r2d2.components.displays");
            board.getClassPool().importPackage("fr.upmc.r2d2.components.controllers");
            board.getClassPool().importPackage("fr.upmc.dtgui.annotations.VariationType");
            
            StringBuilder gpanels = new StringBuilder();
            
            for(Entry<String, StringBuffer> gpanel : groupPanels.entrySet())
                gpanels.append(gpanel.getValue().toString());
            
            Utils.log(gpanels.toString());
            
            board.addMethod(CtMethod.make("public void createPanels() { "+ gpanels.toString() +" }", board));
            board.writeFile();
        }
        
        /*******************************************************
         * OUTILS
         *******************************************************/
        
        /**
         * @return true si le robot a des sensors
         */
        public boolean hasSensors() {
            return robot.hasAnnotation(WithSensors.class);
        }
        
        /**
         * @return true si le robot a des actuators
         */
        public boolean hasActuators() {
            return robot.hasAnnotation(WithActuators.class);
        }

        /**
         * Créer une nouvelle classe de DataQueue
         * 
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
            cons.setBody("this.robot = $1;");
            
            dataQueue.addConstructor(cons);

            return dataQueue;

        }
        
        /*******************************************************
         * Debug
         *******************************************************/        

        /**
         * Uniquement dans le but d'attraper les appels au make et ainsi de
         * faire du déboguage
         * 
         * @param proxy
         * @param method
         * @param args
         * @return
         * @throws Throwable 
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("make")) {
                Block.print("> make " + robot.getSimpleName() + " data queues :");
                Block.print("\tpackage " + robot.getPackageName());
            }
            return method.invoke(this, args);
        }
    }
    
}
