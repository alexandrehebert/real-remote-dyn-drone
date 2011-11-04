package fr.upmc.r2d2.mains;

import fr.upmc.dtgui.example.World;
import fr.upmc.dtgui.tests.AnotherLittleRobot;
import fr.upmc.dtgui.tests.FirstLittleRobot;
import fr.upmc.dtgui.robot.Robot;
import fr.upmc.r2d2.tools.Utils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    private Robot first, another;
    
    private static final boolean SHOW_UI = false;
    
    public static void main(String... args) throws Exception {
        // petit bidule pour tout de même évaluer les tests junit malgré javassist
        final MainTests mainTests = new MainTests();
        mainTests.setUp();
        
        Utils.block("Tests", new Utils.Block() {

            @Override
            public void run() {

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
        robotsClassLoader = new Class[] {FirstLittleRobot.class, AnotherLittleRobot.class};
        word = World.class;
    }
    
    @After
    public void tearDown() throws Exception {}
    
    @Test
    public void testRobots() throws Exception {
        first = (Robot) robotsClassLoader[0].getConstructors()[0].newInstance("No 5", 2000.0, 950.0, 45.0);
        another = (Robot) robotsClassLoader[1].getConstructors()[0].newInstance("No 1", 2850.0, 950.0, 135.0, 10.0);
        Class<?> instrumented = Class.forName("fr.upmc.dtgui.robot.InstrumentedRobot");
        assert (Arrays.asList(first.getClass().getInterfaces()).contains(instrumented));
        assert (Arrays.asList(another.getClass().getInterfaces()).contains(instrumented));
    }
    
    @Test
    public void testSensorDataSender() throws Exception {
        Class<?> c = Class.forName("fr.upmc.dtgui.tests.FirstLittleRobotSensorDataSender");
        Object sds = c.getConstructors()[0].newInstance(first);
        System.out.println("> methods");
        for (Method m : sds.getClass().getMethods())
            if (m.getName().equals("run") || m.getName().equals("start"))
                System.out.println("\t" + m.getName());
        System.out.println("> fields");
        for (Field f : sds.getClass().getDeclaredFields())
            System.out.println("\t" + f.getName());
        
        
        
    }
    
}
