package fr.upmc.r2d2.robots;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.r2d2.tools.Utils;
import java.lang.reflect.Method;

/**
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
        try {
            Method actuator = r.getClass().getMethod(d.getKey(), d.getValue().getClass());
            actuator.invoke(r, d.getValue());
        } catch (Exception ex) {
            Utils.print(ex);
        }
    }
}
