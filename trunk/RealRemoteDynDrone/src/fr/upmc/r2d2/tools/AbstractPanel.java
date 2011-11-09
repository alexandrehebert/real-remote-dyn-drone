package fr.upmc.r2d2.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractPanel<J extends JComponent> extends JPanel {
    
    protected J component;
    
    public AbstractPanel() {
        super();
        
        setLayout(new BorderLayout());
        setSize(450, 125);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        
        add(createComponent(), BorderLayout.NORTH);
        
        JLabel displayLabel = new JLabel(createTitle());
        JPanel labelPane = new JPanel();
        labelPane.add(displayLabel);
        super.add(labelPane, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        component.setVisible(aFlag);
    }

    public abstract J createComponent();
    public abstract String createTitle();
    
}
