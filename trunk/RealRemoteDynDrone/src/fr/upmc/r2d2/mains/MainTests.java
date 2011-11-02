package fr.upmc.r2d2.mains;

import fr.upmc.dtgui.example.World;
import fr.upmc.dtgui.tests.AnotherLittleRobot;
import fr.upmc.dtgui.tests.FirstLittleRobot;
import fr.upmc.dtgui.robot.Robot;
import org.junit.Test;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MainTests {
    
    public static void main(String[] args) {
        Class<? extends Robot>[] robotsClassLoader = new Class[] {AnotherLittleRobot.class, FirstLittleRobot.class};
        Class<World> word = World.class;
    }
    
    @Test
    public void testRobots() {
        System.out.println("qskjqsdlkj");
    }
    
}
