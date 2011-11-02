package fr.upmc.r2d2.mains;

import fr.upmc.dtgui.example.World;
import fr.upmc.dtgui.tests.AnotherLittleRobot;
import fr.upmc.dtgui.tests.FirstLittleRobot;
import fr.upmc.dtgui.robot.Robot;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainTests {
    
    private Class<? extends Robot>[] robotsClassLoader;
    private Class<World> word;
    
    private static final boolean SHOW_UI = false;
    
    public static void main(String... args) throws Exception {
        // petit bidule pour tout de même évaluer les tests junit malgré javassist
        MainTests mainTests = new MainTests();
        mainTests.setUp();
        for (Method m : MainTests.class.getMethods())
            for (Annotation a : m.getAnnotations())
                if (a instanceof Test)
                    try {
                        m.invoke(mainTests);
                        System.out.println("> " + m.getName() + " success");
                    } catch (Exception e) {
                        System.out.println("> " + m.getName() + " error : " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
                        e.printStackTrace();
                    }
        // fr.upmc.dtgui.example.WorldTests.main(new String[]{Boolean.toString(SHOW_UI)});
        mainTests.tearDown();
    }
    
    /**
     * Permet le chargement des classes dans Javassist
     */
    @Before
    public void setUp() throws Exception {
        robotsClassLoader = new Class[] {FirstLittleRobot.class, AnotherLittleRobot.class};
        word = World.class;
    }
    
    @After
    public void tearDown() throws Exception {}
    
    @Test
    public void testRobots() throws Exception {
        Robot first = (Robot) robotsClassLoader[0].getConstructors()[0].newInstance("No 5", 2000.0, 950.0, 45.0);
        Robot another = (Robot) robotsClassLoader[1].getConstructors()[0].newInstance("No 1", 2850.0, 950.0, 135.0, 10.0);
    }
    
    @Test
    public void testSensorDataSender() throws Exception {
        Class<?> c = Class.forName("fr.upmc.dtgui.tests.FirstLittleRobotSensorDataSender");
    }
    
}
