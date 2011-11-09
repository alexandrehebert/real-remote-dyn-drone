package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class GroupPanel extends JPanel {

    protected InstrumentedRobot lr;
    protected int height = 0, width = 100;
    private List<ConnectableRobot> panels = new ArrayList();
    
    public GroupPanel(int w, int h) {
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setVisible(true);
    }
    
    protected final void addComponent(AbstractControllerPanel p) {
        this.adjust(p);
        this.add(p);
    }
    
    protected final void addComponent(AbstractDisplayPanel p) {
        this.adjust(p);
        this.add(p);
    }
    
    private void adjust(JPanel p) {
        height += p.getHeight();
        width = (p.getWidth() > width) ? p.getWidth() : width;
    }

    protected final void commit() {
        super.setSize(width, height);
    }
    
}
