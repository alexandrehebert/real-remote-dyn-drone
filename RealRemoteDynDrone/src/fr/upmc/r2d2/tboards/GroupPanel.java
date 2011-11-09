package fr.upmc.r2d2.tboards;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class GroupPanel extends JPanel {

    protected int height = 0, width = 100;

    public GroupPanel(String name, int w, int h) {
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JLabel displayLabel = new JLabel(name);
        JPanel labelPane = new JPanel();
        labelPane.add(displayLabel);
        addComponent(labelPane);
        
        this.setVisible(true);
    }

    protected final void addComponent(JPanel p) {
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
