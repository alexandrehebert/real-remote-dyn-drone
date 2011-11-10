package fr.upmc.r2d2.boards;

import fr.upmc.dtgui.gui.RobotTeleoperationBoard;
import fr.upmc.dtgui.gui.SensorDataReceptorInterface;
import fr.upmc.dtgui.gui.TeleoperationGUI;
import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.Robot;
import java.awt.HeadlessException;
import java.util.HashMap;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class DynGUI extends TeleoperationGUI {

    private static final long serialVersionUID = 1L;

    public DynGUI(
            String panelName,
            int absoluteX,
            int absoluteY,
            int relativeX,
            int relativeY,
            int controlRadius,
            int sizeX,
            int sizeY) throws HeadlessException {
        super(panelName, absoluteX, absoluteY, relativeX, relativeY,
                controlRadius, sizeX, sizeY);
        /* ici on initialisera la map boards avec Javassist */
        
        /*
         * boards.put(RobotA.class, new RobotATeleoperationBoard(this, sizeX - 50));
         * 
         */
    }
    
    private HashMap<Class<? extends Robot>, RobotTeleoperationBoard> boards = new HashMap();

    public RobotTeleoperationBoard createBoard(InstrumentedRobot lr) {
        RobotTeleoperationBoard board = boards.get(lr.getClass());
        
        if (board == null) {
            System.out.println("Unknown type of robot : "
                    + lr.getClass().getCanonicalName());
            System.exit(1);
        }
        
        return board;
    }

    public SensorDataReceptorInterface createSensorDataReceptor(
            InstrumentedRobot lr,
            RobotTeleoperationBoard board) {
        
        if (boards.containsKey(lr.getClass())){
            // TODO: create an exception type
            System.out.println("Unknown type of robot : "
                    + lr.getClass().getCanonicalName());
            System.exit(1);
        }
        
        return board.makeSensorDataReceptor(
                    this.positionDisplay, lr.getSensorDataQueue(),
                    this.absoluteX, this.absoluteY, this.controlRadius);
    }
}