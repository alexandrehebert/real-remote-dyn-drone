package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractPanel extends JPanel {

    protected InstrumentedRobot lr;
    protected AbstractControllerPanel acp;
    protected AbstractDisplayPanel adp;
    
    public AbstractPanel(int w, int h) {
        super.setLayout(new BorderLayout());
        super.setSize(w, h);
        this.setVisible(true);
    }
    
    protected final void setController(AbstractControllerPanel acp) {
        this.acp = acp;
    }
    
    protected final void setDisplay(AbstractDisplayPanel adp) {
        this.adp = adp;
    }
    
    protected final boolean hasController() {
        return acp != null;
    }
    
    protected final boolean hasDisplay() {
        return adp != null;
    }

    public void disconnectRobot(InstrumentedRobot lr) {
        this.lr = null;
        if (!hasController()) return;
        this.acp.disconnectRobot(lr);
    }

    public void connectRobot(InstrumentedRobot lr) {
        this.lr = lr;
        if (!hasController()) return;
        this.acp.connectRobot(lr);
    }

    public void update(SensorData sd) {
        if (!hasDisplay()) return;
        this.adp.update(sd);
    }
    
    protected final void commit() {
        if (hasDisplay()) this.add(adp, BorderLayout.NORTH);
        if (hasController()) this.add(acp, BorderLayout.SOUTH);
    }
    
}
