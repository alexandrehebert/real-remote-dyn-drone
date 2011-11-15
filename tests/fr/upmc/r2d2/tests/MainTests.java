package fr.upmc.r2d2.tests;

import fr.upmc.dtgui.example.World;
import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.tests.AnotherLittleRobot;
import fr.upmc.dtgui.tests.LittleRobot;
import fr.upmc.dtgui.robot.Robot;
import fr.upmc.r2d2.boards.DynGUI;
import fr.upmc.r2d2.mains.MainJavassist;
import fr.upmc.r2d2.robots.ActuatorCommand;
import fr.upmc.r2d2.robots.MessageData;
import fr.upmc.r2d2.tools.Utils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainTests extends TestCase {
    
    private Class<? extends Robot>[] robotsClassLoader;
    private Robot[] robots = new Robot[2];
    private Class<World> wordClassLoader;
    private Class<DynGUI> guiClassLoader;
    private BlockingQueue testQueue;
    
    private static final int FIRST = 0, ANOTHER = 1;
    
    private static final Map<Integer, Entry<TestDetails, Method>> testsMethods = new HashMap();
    
    /**
     * On simule le comportement de JUnit en récupérant les annotations et en traitant
     * les différents tests de manière séquentielle
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String... args) throws Exception {
        // petit bidule pour tout de même évaluer les tests junit malgré javassist
        final MainTests mainTests = new MainTests();
        mainTests.setUp();
        
        Utils.block("Tests", new Utils.Block() {
            
            @Override
            public void run() {

                /**
                 * première passe, on recense la liste des méthodes de Test
                 * et ce afin de les trier en fonction et de l'ordre stipulé dans
                 * l'annotation TestDetails
                 */
                for (Method m : MainTests.class.getMethods()) {
                    for (Annotation a : m.getAnnotations()) {
                        if (a instanceof TestDetails)
                            testsMethods.put(((TestDetails)a).order(), new HashMap.SimpleEntry(a, m));
                    }
                }
                
                /**
                 * On parcourt la liste des méthodes, triées grâce au TreeMap
                 */
                for (Object o : (new TreeMap(testsMethods)).entrySet()) {
                    
                    Entry<TestDetails, Method> v = (Entry<TestDetails, Method>) ((Entry) o).getValue();
                    Method m = v.getValue();
                    TestDetails td = v.getKey();
                    
                    try {
                        // lancement du test
                        go(m.getName(), td.description());
                        m.invoke(mainTests);
                        pass();
                        // si on arrive ici, le test est un succes
                    } catch (Exception e) {
                        err(e);
                    } finally {
                        print();
                        flush();
                    }
                    
                }
                
                print(">>> nombre d'erreurs : " + errors());
                
            }
            
        });
        
        // fr.upmc.dtgui.example.WorldTests.main(new String[]{Boolean.toString(SHOW_UI)});
        mainTests.tearDown();
    }
    
    /**
     * Permet le chargement des classes dans Javassist
     */
    @Before
    public void setUp() throws Exception {
        robotsClassLoader = new Class[] {LittleRobot.class, AnotherLittleRobot.class};
        guiClassLoader = DynGUI.class;
        wordClassLoader = World.class;
        robots[FIRST] = (Robot) robotsClassLoader[FIRST].getConstructors()[0].newInstance("No 5", 2000.0, 950.0, 45.0);
        robots[ANOTHER] = (Robot) robotsClassLoader[ANOTHER].getConstructors()[0].newInstance("No 1", 2850.0, 950.0, 135.0, 10.0);
    }
    
    @After
    public void tearDown() throws Exception {}
    
    @Test
    @TestDetails(order=0,description="XXXRobot implements InstrumentedRobot ?")
    public void testRobotsInheritance() throws Exception {
        
        Class<?> instrumented = Class.forName("fr.upmc.dtgui.robot.InstrumentedRobot");
        
        for (Robot r : robots) {
            assertTrue(r.getRobotName() + " : héritage inexistant", Arrays.asList(r.getClass().getInterfaces()).contains(instrumented));
            System.out.println("\t" + r.getRobotName() + " implémente instrumented");
        }
        
    }
    
    @Test
    @TestDetails(order=1,description="XXXSensorDataSender créé ?")
    public void testSensorDataSender() throws Exception {
        
        for (Robot r : robots) {
            
            Class<?> c = Class.forName(r.getClass().getName() + MainJavassist.RobotMaker.SENDER_EXT);
            System.out.println("class " + c.getSimpleName() + " :");
            System.out.print("\t> methods :");
            for (Method m : c.getMethods())
                if (m.getName().equals("run") || m.getName().equals("start"))
                    System.out.print(" " + m.getName());
            System.out.println();
            System.out.print("\t> fields :");
            for (Field f : c.getDeclaredFields())
                System.out.print(" " + f.getName());
            System.out.println();
            
        }
        
    }
    
    @Test
    @TestDetails(order=2,description="Queues de communication fonctionnelles ?")
    public void testSensorQueues() throws Exception {
        
        Robot r = robots[FIRST];
        Class<?> c = Class.forName(r.getClass().getName() + MainJavassist.RobotMaker.SENDER_EXT);
        System.out.println("instanciation de " + c.getSimpleName());
        Object sdsTest = c.getConstructors()[0].newInstance(r);
        Field f = sdsTest.getClass().getField("dataQueue");
        assertTrue("type de dataQueue invalide", f.getType() == BlockingQueue.class);
        
        try {
            ((Thread)sdsTest).start();
            testQueue = (BlockingQueue)f.get(sdsTest);
            Vector<MessageData> mds = new Vector();
            int cpt = 0;
            testQueue.add(new MessageData("TEST_GROUP", "testMethod", new String("TestValue")));
            while ((testQueue.isEmpty() || testQueue.drainTo(mds) > 0) && cpt++ < 10) {
                Thread.sleep(20);
                System.out.println("\t> drain " + cpt + " : " + mds.size() + " messages");
                for (MessageData md : mds)
                    System.out.println("\t\t" + md);
                mds.clear();
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            // dans tous les cas on stoppe le thread de la queue
            try { ((Thread)sdsTest).interrupt();}
            catch (Exception e) {}
        }
        
    }
    
    @Test
    @TestDetails(order=3,description="Envoi de commandes fonctionnel ?")
    public void testCommandQueues() throws Exception {
        
        LittleRobot r = ((LittleRobot)robots[FIRST]);
        testQueue.clear();
        double d = r.getSpeed();
        ActuatorCommand.performOn((InstrumentedRobot)robots[FIRST], new MessageData("TEST_GROUP", "setSpeed", new Double(0)));
        double dpost = r.getSpeed();
        assertTrue(d != dpost && dpost == 0);
        System.out.println("la vitesse a été correctement modifiée");
        
    }
    
}
