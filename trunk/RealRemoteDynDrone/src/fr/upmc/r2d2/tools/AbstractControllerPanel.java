package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractControllerPanel extends JPanel {
    
    protected EventListener el;
    protected InstrumentedRobot r;
    protected JComponent view;
    
    public void disconnectRobot() {
        this.r = null;
        disconnect(el);
    }
    
    public void connectRobot(InstrumentedRobot r) {
        this.r = r;
        el = connect(r.getActuatorDataQueue());
    }
    
    public abstract EventListener connect(BlockingQueue bq);
    public abstract void disconnect(EventListener el);
    
    public abstract JComponent getComponent();
}
