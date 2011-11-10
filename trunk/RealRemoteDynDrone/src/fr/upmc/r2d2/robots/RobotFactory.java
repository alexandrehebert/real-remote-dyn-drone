package fr.upmc.r2d2.robots;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.r2d2.tools.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RobotFactory {
    
    public static InstrumentedRobot make(Class robotClass, Object... args) {
        List<Class> cargs = new ArrayList();
        
        for(Object arg : args)
            cargs.add(arg.getClass());
        
        try {
            return (InstrumentedRobot) robotClass.getConstructor(cargs.toArray(new Class[] {})).newInstance(args);
        } catch (Exception ex) {
            Utils.print(new Exception("Impossible de construire de robot " + robotClass.getName() +" avec ces arguments", ex));
        }
        
        return null;
    }
    
}
