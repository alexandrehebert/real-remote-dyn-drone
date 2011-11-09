package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.example.robot.LittleRobot;
import fr.upmc.dtgui.robot.RobotActuatorCommand;
import fr.upmc.r2d2.tools.AbstractTeleoperationBoard.ActuatorDataSender;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RealControllerPanel extends AbstractControllerPanel {
    
    private JSlider slider;
    
    public RealControllerPanel() {
        slider = new JSlider();
    }

    @Override
    public EventListener connect(BlockingQueue bq) {
        return new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                double newSteeringAngle = source.getValue();
                final RobotActuatorCommand sc =
                        LittleRobot.makeSteeringChange(newSteeringAngle);
                (new ActuatorDataSender(sc, bq)).start();
            }
        }
    }

    @Override
    public void disconnect(EventListener el) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    class SteeringActuatorDataListener implements ChangeListener {

        final protected BlockingQueue<RobotActuatorCommand> commandQueue;

        public SteeringActuatorDataListener(
                BlockingQueue<RobotActuatorCommand> commandQueue) {
            super();
            this.commandQueue = commandQueue;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            double newSteeringAngle = source.getValue();
            final RobotActuatorCommand sc =
                    LittleRobot.makeSteeringChange(newSteeringAngle);
            (new ActuatorDataSender(sc, this.commandQueue)).start();
        }
    }
    
}
