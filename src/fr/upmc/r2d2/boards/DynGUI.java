package fr.upmc.r2d2.boards;

import fr.upmc.dtgui.gui.RobotTeleoperationBoard;
import fr.upmc.dtgui.gui.SensorDataReceptorInterface;
import fr.upmc.dtgui.gui.TeleoperationGUI;
import fr.upmc.dtgui.robot.InstrumentedRobot;
import fr.upmc.dtgui.robot.Robot;
import java.awt.HeadlessException;
import java.util.HashMap;

/**
 * DynGUI héberge l'ensemble du code d'instanciation des différents TBoards
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class DynGUI extends TeleoperationGUI {
    
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    private HashMap<Class<? extends Robot>, RobotTeleoperationBoard> boards = new HashMap();
    
    /**
     * Création d'un DynGui et ajout de l'ensemble des boards associés aux robots
     * 
     * @param panelName
     * @param absoluteX
     * @param absoluteY
     * @param relativeX
     * @param relativeY
     * @param controlRadius
     * @param sizeX
     * @param sizeY
     * @throws HeadlessException 
     */
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
        /* 
         * /!\ ici on initialisera la map boards avec Javassist /!\
         */
    }
    
    /**
     * Recherche et créé le TBoard associé au robot passé en paramètre
     * 
     * @param r robot correspondant au board
     * @return 
     */
    public RobotTeleoperationBoard createBoard(InstrumentedRobot r) {
        return checkBoard(r.getClass());
    }

    public SensorDataReceptorInterface createSensorDataReceptor(
            InstrumentedRobot r,
            RobotTeleoperationBoard board) {
        checkBoard(r.getClass());
        return board.makeSensorDataReceptor(
                    this.positionDisplay, r.getSensorDataQueue(),
                this.absoluteX, this.absoluteY, this.controlRadius);
    }

    /**
     * Teste si le robot est supporté par le DynGUI
     * Stoppe l'execution si le TBoard n'est pas trouvé
     * 
     * @param r classe de robot testée
     * @return 
     */
    public RobotTeleoperationBoard checkBoard(Class<? extends Robot> r) {
        if (!boards.containsKey(r)) {
            System.out.println("$ unknown type of robot : "
                    + r.getCanonicalName());
            System.exit(1);
        }
        return boards.get(r);
    }
    
}
