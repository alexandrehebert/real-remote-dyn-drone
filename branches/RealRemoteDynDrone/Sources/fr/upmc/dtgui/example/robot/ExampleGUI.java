//	ExampleGUI.java --- 
package fr.upmc.dtgui.example.robot;

import java.awt.HeadlessException;

import fr.upmc.dtgui.example.gui.AnotherLittleRobotTeleoperationBoard;
import fr.upmc.dtgui.example.gui.LittleRobotTeleoperationBoard;
import fr.upmc.dtgui.gui.RobotTeleoperationBoard;
import fr.upmc.dtgui.gui.SensorDataReceptorInterface;
import fr.upmc.dtgui.gui.TeleoperationGUI;
import fr.upmc.dtgui.robot.InstrumentedRobot;

public class ExampleGUI extends TeleoperationGUI {

    private static final long serialVersionUID = 1L;

    public ExampleGUI(
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
    }

    public RobotTeleoperationBoard createBoard(InstrumentedRobot lr) {
        RobotTeleoperationBoard board = null;
        if (lr instanceof LittleRobot) {
            board =
                    new LittleRobotTeleoperationBoard(this, this.sizeX - 50);
        } else if (lr instanceof AnotherLittleRobot) {
            board =
                    new AnotherLittleRobotTeleoperationBoard(this, this.sizeX - 50);
        } else {
            // TODO: create an exception type
            System.out.println("Unknown type of robot : "
                    + lr.getClass().getCanonicalName());
            System.exit(1);
        }
        return board;
    }

    public SensorDataReceptorInterface createSensorDataReceptor(
            InstrumentedRobot lr,
            RobotTeleoperationBoard board) {
        SensorDataReceptorInterface sdr = null;
        if (lr instanceof LittleRobot) {
            sdr = board.makeSensorDataReceptor(
                    this.positionDisplay, lr.getSensorDataQueue(),
                    this.absoluteX, this.absoluteY, this.controlRadius);
        } else if (lr instanceof AnotherLittleRobot) {
            sdr = board.makeSensorDataReceptor(
                    this.positionDisplay, lr.getSensorDataQueue(),
                    this.absoluteX, this.absoluteY, this.controlRadius);
        } else {
            // TODO: create an exception type
            System.out.println("Unknown type of robot : "
                    + lr.getClass().getCanonicalName());
            System.exit(1);
        }
        return sdr;
    }
}

// $Id$