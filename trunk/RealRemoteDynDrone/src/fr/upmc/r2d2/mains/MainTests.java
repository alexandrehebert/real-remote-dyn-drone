package fr.upmc.r2d2.mains;

import fr.upmc.dtgui.example.World;
import fr.upmc.dtgui.tests.AnotherLittleRobot;
import fr.upmc.dtgui.tests.LittleRobot;
import fr.upmc.dtgui.robot.Robot;
import fr.upmc.r2d2.boards.DynGUI;
import fr.upmc.r2d2.tools.TestDetails;
import fr.upmc.r2d2.tools.Utils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainTests {
    
    private Class<? extends Robot>[] robotsClassLoader;
    private Robot[] robots = new Robot[2];
    private Class<World> wordClassLoader;
    private Class<DynGUI> guiClassLoader;
    
    private static final int FIRST = 0, ANOTHER = 1;
    
    private static final Map<Integer, Entry<TestDetails, Method>> testsMethods = new HashMap();
    private static int testsOrder = 0;
    
    public static void main(String... args) throws Exception {
        // petit bidule pour tout de même évaluer les tests junit malgré javassist
        final MainTests mainTests = new MainTests();
        mainTests.setUp();
        
        Utils.block("Tests", new Utils.Block() {
            
            @Override
            public void run() {

                for (Method m : MainTests.class.getMethods()) {
                    for (Annotation a : m.getAnnotations()) {
                        if (a instanceof TestDetails)
                            testsMethods.put(((TestDetails)a).order(), new HashMap.SimpleEntry(a, m));
                    }
                }
                
                for (Object o : (new TreeMap(testsMethods)).entrySet()) {
                    Entry<TestDetails, Method> v = (Entry<TestDetails, Method>) ((Entry) o).getValue();
                    Method m = v.getValue();
                    TestDetails td = v.getKey();
                    try {
                        go(m.getName(), td.description());
                        m.invoke(mainTests);
                        pass();
                    } catch (Exception e) {
                        err(e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
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
            assert (Arrays.asList(r.getClass().getInterfaces()).contains(instrumented));
            System.out.println("\t" + r.getRobotName() + " implémente instrumented");
        }
        
    }
    
    @Test
    @TestDetails(order=3,description="XXXSensorDataSender créé ?")
    public void testSensorDataSender() throws Exception {
        
        for (Robot r : robots) {
            
            Class<?> c = Class.forName(r.getClass().getName() + MainJavassist.RobotMaker.SENDER_EXT);
            Object sds = c.getConstructors()[0].newInstance(r);
            System.out.println("class " + c.getSimpleName() + " :");
            System.out.print("\t> methods :");
            for (Method m : sds.getClass().getMethods())
                if (m.getName().equals("run") || m.getName().equals("start"))
                    System.out.print(" " + m.getName());
            System.out.println();
            System.out.print("\t> fields :");
            for (Field f : sds.getClass().getDeclaredFields())
                System.out.print(" " + f.getName());
            System.out.println();
            
        }
        
    }
    
    @Test
    @TestDetails(order=2,description="Queues de communication fonctionnelles ?")
    public void testQueues() throws Exception {
        
        Class<?> c = Class.forName(robots[0].getClass().getName() + MainJavassist.RobotMaker.SENDER_EXT);
        System.out.println("instanciation de " + c.getSimpleName());
        c.getConstructors()[0].newInstance(robots[0]);
        
    }
    
}
