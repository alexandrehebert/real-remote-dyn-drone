package fr.upmc.r2d2.mains;

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

    public static void main(String[] args) throws Throwable {
        LOADER.addTranslator(new RobotTranslator(), "@fr.upmc.dtgui.annotations.WithSensors");
//        LOADER.addTranslator(new RobotWithActuatorsTranslator(), "@fr.upmc.dtgui.annotations.WithActuators");
        // LOADER.addTranslator(new RobotTranslator(), "fr.upmc.dtgui.robot.Robot");
        LOADER.addTranslator(new WorldTranslator(), "fr.upmc.dtgui.example.World");
        LOADER.run("fr.upmc.r2d2.mains.MainTests");
        //LOADER.run("fr.upmc.r2d2.tests.MainTests");
    }
    
    private static class RobotTranslator implements AssistantLoader.ISimpleTranslator {

        @Override
        public void onLoad(ClassPool cp, String string, final CtClass cc) throws Exception {
            
            Utils.block(cc.getSimpleName(), new Utils.Block() {

                @Override
                public void run() throws Exception {
                    final Makeable maker = (Makeable) Proxy.newProxyInstance(
                            RobotMaker.class.getClassLoader(), 
                            new Class[] {Makeable.class}, 
                            new RobotMaker(cc));
                    
                    WALKER.walk(cc, new WalkAnnotations.IAnnotationWalker() {
                        @Override
                        public void walk(CtMethod m, Annotation a) throws Exception {
                             maker.processAnnotation(m, m.getName().substring(3), a);
                        }
                    });

                    maker.make();
                }
                
            });
            
        }
    }

    private static class WalkAnnotations {

        public interface IAnnotationWalker {
            public void walk(CtMethod m, Annotation a) throws Exception;
        }

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
    
    public static interface Makeable extends InvocationHandler {
        public void make() throws Exception;
        public void processAnnotation(CtMethod m, String name, Annotation sensor) throws Exception;
    }
    
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
        
        @Override        
        public void processAnnotation(CtMethod m, String name, Annotation a) throws Exception {
            Method process;
            
            try {
                process = getClass()
                    .getMethod("processAnnotation", CtMethod.class, String.class, a.annotationType());
            } catch (Throwable ex) { return; } // si on ne trouve pas la méthode tant pis, pas besoin de faire remonter d'exception
            
            AnnotationPrinter ap = new AnnotationPrinter(a);
            System.out.println("\t" + ap + " " + name);
            
            switch (ap.getType()) {
                case ACTUATOR: actuators.put(m, a); break;
                case SENSOR : sensors.put(m, a); break;
            }
            
            process.invoke(this, m, name, a); 
        }
        
        public void processAnnotation(CtMethod m, String name, RealSensorData sensor) {
            tmpQueue.append(("dataQueue.add(new fr.upmc.r2d2.tools.SensorData.RealSensorCapsule(new Double(robot.get" + name + "()), null));"));
        }
        
        public void processAnnotation(CtMethod m, String name, RealActuatorData sensor) {
            // tmpQueue.append(("dataQueue.add(new fr.upmc.r2d2.tools.SensorData.BooleanSensorCapsule(new Boolean(robot.get" + name + "()), sensor));"));
        }
        
        @Override
        public void make() throws Exception {
            if (hasSensors()) {
                System.out.println("\tclass " + robot.getSimpleName() + SENDER_EXT);
                makeSensors();
            }
            if (hasActuators()) {
                System.out.println("\tclass " + robot.getSimpleName() + RECEPTOR_EXT);
                makeActuators();  
            }
            makeRobot();
        }
        
        public boolean hasSensors() {
            return robot.hasAnnotation(WithSensors.class);
        }
        
        public boolean hasActuators() {
            return robot.hasAnnotation(WithActuators.class);
        }
        
        public void makeSensors() throws Exception {
            
            dataSender = makeDataQueue(SENDER_EXT, sensors.size());
            dataSender.addField(CtField.make("private int sleep = 100;", dataSender));
            
            // construction du code des méthodes et constructeurs
            String run = Utils.readSnippet(SENDER_EXT + ".run").replaceFirst("#TMPQUEUE#", tmpQueue.toString());
            
            // ajout des méthodes
            dataSender.addMethod(CtMethod.make(Utils.readSnippet(SENDER_EXT + ".start"), dataSender));
            dataSender.addMethod(CtMethod.make(run, dataSender));
            
            dataSender.writeFile();
            
        }
        
        public void makeActuators() throws Exception {
            
            dataReceptor = makeDataQueue(RECEPTOR_EXT, 1);
            dataReceptor.writeFile();
            
        }
        
        private CtClass makeDataQueue(String ext, int size) throws Exception {
            
            CtClass dataQueue = pool.makeClass(
                    // getName doit retourner le nom complet de la classe + package
                    robot.getName() + ext,
                    LOADER.getCtClass(pool, "java.lang.Thread"));
            
            dataQueue.addField(CtField.make("public final BlockingQueue dataQueue = new ArrayBlockingQueue(" + size + ");", dataQueue));
            dataQueue.addField(CtField.make("private " + robot.getName() + " robot;", dataQueue));
            
            CtConstructor cons = new CtConstructor(new CtClass[] {robot}, dataQueue);
            cons.callsSuper();
            cons.setBody("this.robot = robot;");
            dataQueue.addConstructor(cons);
            
            return dataQueue;
            
        }
        
        public void makeRobot() throws Exception {
            
            // implant des classes sender & receptor dans le robot
            robot.setInterfaces(new CtClass[] {LOADER.getCtClass(pool, "fr.upmc.dtgui.robot.InstrumentedRobot")});
            
            if (hasSensors()) {
                robot.addField(CtField.make("private " + robot.getName() + SENDER_EXT + " sds;", robot));
                robot.getDeclaredMethod("start").insertBefore("sds.start();");
                robot.addMethod(CtMethod.make(Utils.readSnippet("Robot.getSensorDataQueue"), robot));
                robot.getConstructors()[0].insertAfter("sds = new " + robot.getName() + SENDER_EXT + "(this);");
            }
            
            if (hasActuators()) {
                robot.addField(CtField.make("private " + robot.getName() + RECEPTOR_EXT + " adr;", robot));
                robot.getDeclaredMethod("start").insertBefore("adr.start();");
                robot.addMethod(CtMethod.make(Utils.readSnippet("Robot.getActuatorDataQueue"), robot));
                robot.getConstructors()[0].insertAfter("adr = new " + robot.getName() + RECEPTOR_EXT + "(this);");
            }
            
            robot.writeFile();
            
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("make")) {
                System.out.println("> make " + robot.getSimpleName() + " data queues :");
                System.out.println("\tpackage " + robot.getPackageName());
            }
            return method.invoke(this, args);
        }
        
    }

    private static class WorldTranslator implements AssistantLoader.ISimpleTranslator  {
        
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