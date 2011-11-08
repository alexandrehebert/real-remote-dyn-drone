package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractControllerPanel extends JPanel {
    
    protected InstrumentedRobot lr;
    protected EventListener el;
    
    public void disconnectRobot(InstrumentedRobot lr) {
        disconnect(el);
        this.lr = null;
    }

    public void connectRobot(InstrumentedRobot lr) {
        this.lr = lr;
        el = connect(lr.getActuatorDataQueue());
    }
    
    public abstract EventListener connect(BlockingQueue bq);
    public abstract void disconnect(EventListener el);
    
}
