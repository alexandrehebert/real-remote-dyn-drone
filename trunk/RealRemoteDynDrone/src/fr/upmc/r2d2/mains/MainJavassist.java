package fr.upmc.r2d2.mains;

import fr.upmc.dtgui.annotations.BooleanActuatorData;
import fr.upmc.dtgui.annotations.BooleanSensorData;
import fr.upmc.dtgui.annotations.IntegerActuatorData;
import fr.upmc.dtgui.annotations.IntegerSensorData;
import fr.upmc.dtgui.annotations.RealActuatorData;
import fr.upmc.dtgui.annotations.RealSensorData;
import fr.upmc.dtgui.annotations.WithActuators;
import fr.upmc.dtgui.annotations.WithSensors;
import fr.upmc.r2d2.tools.AssistantLoader;
import fr.upmc.r2d2.tools.Utils;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainJavassist {

    private static final WalkAnnotations WALKER = new WalkAnnotations();
    private static final boolean SHOW_UI = false;
    private static final AssistantLoader LOADER = new AssistantLoader();

    public static void main(String[] args) throws Throwable {
        LOADER.addTranslator(new RobotTranslator(), "@fr.upmc.dtgui.annotations.WithSensors");
//        LOADER.addTranslator(new RobotWithActuatorsTranslator(), "@fr.upmc.dtgui.annotations.WithActuators");
        // LOADER.addTranslator(new RobotTranslator(), "fr.upmc.dtgui.robot.Robot");
        LOADER.addTranslator(new WorldTranslator(), "fr.upmc.r2d2.tests.World");
        LOADER.run("fr.upmc.r2d2.tests.WorldTests", new String[]{Boolean.toString(SHOW_UI)});
        //l.run("fr.upmc.r2d2.mains.MainTests");
    }

    private static class RobotTranslator implements AssistantLoader.ISimpleTranslator {

        @Override
        public void onLoad(ClassPool cp, String string, final CtClass cc) throws Exception {
            
            System.out.println("-------------------------------------------------");
            System.out.println("|\t" + cc.getSimpleName());
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - -");
            
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
            System.out.println("-------------------------------------------------");
            System.out.println();
            
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
        /**
         * SensorDataSender
         */
        private StringBuffer tmpQueue = new StringBuffer();
        private List<Annotation> sensors = new ArrayList();
        private String run = Utils.readSnippet("SensorDataSender.run");
        private String start = Utils.readSnippet("SensorDataSender.start");
        private CtClass robot;
        private CtClass dataSender;
        private ClassPool pool;
        
        /**
         * ActuatorDataReceiver
         */        
        
        public RobotMaker(CtClass robot) {
            this.robot = robot;
            this.pool = robot.getClassPool();
            pool.importPackage("java.util.concurrent");
            pool.importPackage("java.lang.Thread");
        }
        
        @Override        
        public void processAnnotation(CtMethod m, String name, Annotation sensor) throws Exception {
            Method addSensor;
            
            try {
                addSensor = getClass()
                .getMethod("processAnnotation", CtMethod.class, String.class, sensor.annotationType());
            } catch (Throwable ex) { return; } // si on ne trouve pas la méthode tant pis, pas besoin de faire remonter d'exception
            
            addSensor.invoke(this, m, name, sensor); 
            sensors.add(sensor);
        }

        public void processAnnotation(CtMethod m, String name, RealSensorData sensor) {
            // pas tout à fait juste, ne fonctionne pas avec les sensors X & Y
            // provisoire :
            //if (name.equals("SteeringAngle")) name = "Steering";
            final StringBuffer s = new StringBuffer();
            try {
            ObjectOutputStream oos = new ObjectOutputStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    s.append((char)b);
                }
            });
                oos.writeObject(sensor);
                oos.close();
            } catch (Exception e) {
            } finally {
                System.out.println(s.toString());
            }
            tmpQueue.append(("dataQueue.add(new fr.upmc.r2d2.tools.SensorData.RealSensorCapsule(new Double(robot.get" + name + "()), ));"));

        }
        
        public void processAnnotation(CtMethod m, String name, BooleanSensorData sensor) {
            // pas tout à fait juste, ne fonctionne pas avec les sensors X & Y
            // provisoire :
            //if (name.equals("SteeringAngle")) name = "Steering";
            // tmpQueue.append(("dataQueue.add(new fr.upmc.r2d2.tools.SensorData.BooleanSensorCapsule(new Boolean(robot.get" + name + "()), sensor));"));
        }
        
        @Override
        public void make() throws Exception {
            if (robot.hasAnnotation(WithSensors.class)) {
                System.out.println("> begin makeSensors");
                makeSensors();
                System.out.println("< end");
            }
            if (robot.hasAnnotation(WithActuators.class)) {
                System.out.println("> begin makeActuators");
                makeActuators();        
                System.out.println("< end");    
            }
            makeRobot();
        }
        
        public void makeSensors() throws Exception {
            // création de la ctclass
            dataSender = pool.makeClass(
                    // getName doit retourner le nom complet de la classe + package
                    robot.getName() + "SensorDataSender",
                    LOADER.getCtClass(pool, "java.lang.Thread"));
            
            // ajout des champs 
            dataSender.addField(CtField.make("public final BlockingQueue dataQueue = new ArrayBlockingQueue(" + sensors.size() + ");", dataSender));
            dataSender.addField(CtField.make("private " + robot.getName() + " robot;", dataSender));
            
            // construction du code des méthodes et constructeurs
            run = run.replaceFirst("#TMPQUEUE#", tmpQueue.toString());
            CtConstructor cons = new CtConstructor(new CtClass[] {robot}, dataSender);
            cons.callsSuper();
            cons.setBody("this.robot = robot;");
            
            // ajout des méthodes
            dataSender.addMethod(CtMethod.make(start, dataSender));
            dataSender.addMethod(CtMethod.make(run, dataSender));
            dataSender.addConstructor(cons);
            
            dataSender.writeFile();
            
        }
        
        public void makeActuators() throws Exception {
            
        }
        
        public void makeRobot() throws Exception {
            
            // implant de la classe dans le robot
            robot.setInterfaces(new CtClass[] {LOADER.getCtClass(pool, "fr.upmc.dtgui.robot.InstrumentedRobot")});
            robot.addField(CtField.make("private " + robot.getName() + "SensorDataSender sds;", robot));
            
            // construction du code des méthodes et constructeurs du robot
            robot.getDeclaredMethod("start").insertBefore("sds.start();");
            robot.addMethod(CtMethod.make(Utils.readSnippet("Robot.getSensorDataQueue"), robot));
            robot.getConstructors()[0].insertAfter("sds = new " + robot.getName() + "SensorDataSender(this);");
            
            robot.writeFile();
            
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("make"))
                System.out.println("makin " + robot.getName() + "...");
            return method.invoke(this, args);
        }
        
    }

    private static class WorldTranslator implements AssistantLoader.ISimpleTranslator  {
        
        @Override
        public void onLoad(ClassPool cp, String string, CtClass c) throws Exception {
            System.out.println("Make The World !!!");
        }
    }
    
}