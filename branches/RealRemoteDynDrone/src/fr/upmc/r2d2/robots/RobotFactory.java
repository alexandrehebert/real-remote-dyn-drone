package fr.upmc.r2d2.robots;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.r2d2.tools.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 * @see RobotFactory#make(java.lang.Class, java.lang.Object[]) 
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RobotFactory {
    
    /**
     * Cette méthode à pour objectif d'abstraire du programme les déclaration explicites
     * des instanciations des diverses classes de robot.
     * 
     * Ainsi, lorsque les classes sont modifiées par Javassist, l'instanciation par
     * reflexion fonctionne, tandis que si l'on essaye d'instancier des robots
     * explicitement, une erreur de compilation survient : le robot n'est pas
     * encore instrumented
     * 
     * @param robotClass
     * @param args
     * @return null si le robot n'est pas instancié correctement
     */
    @SuppressWarnings("UseSpecificCatch")
    public static InstrumentedRobot make(Class robotClass, Object... args) {
        List<Class> cargs = new ArrayList();
        
        for(Object arg : args) {
            /**
             * solution temporaire pour pallier au type primitif
             */
            cargs.add(Utils.class2primitive(arg.getClass()));
        }
        
        try {
            return (InstrumentedRobot) robotClass.getConstructor(cargs.toArray(new Class[] {})).newInstance(args);
        } catch (Exception ex) {
            Utils.print(new Exception("Impossible de construire de robot " + robotClass.getName() +" avec ces arguments", ex));
        }
        
        return null;
    }
    
}
