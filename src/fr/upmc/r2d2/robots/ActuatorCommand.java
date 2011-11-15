package fr.upmc.r2d2.robots;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.r2d2.tools.Utils;
import java.lang.reflect.Method;

/**
 * Execution d'une commande reçue par un robot
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class ActuatorCommand {
    private InstrumentedRobot r;

    public ActuatorCommand(InstrumentedRobot r) {
        this.r = r;
    }

    public void perform(MessageData d) {
        performOn(r, d);
    }
    
    public static void performOn(InstrumentedRobot r, MessageData d) {
        System.out.println("$ execute command " + d + " on " + r.getRobotName());
        
        try {
            Method actuator = r.getClass().getMethod(d.getKey(), Utils.class2primitive(d.getValue().getClass()));
            actuator.invoke(r, d.getValue());
        } catch (Exception ex) {
            Utils.print(ex);
        }
    }

}
