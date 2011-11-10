package fr.upmc.r2d2.components;

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
    private String groupName;
    private String methodName;
    protected double minRate, maxRate;    
    
    public AbstractPanel(String groupName, String methodName, double minRate, double maxRate) {
        super();
        
        this.groupName = groupName;
        this.methodName = methodName;
        this.minRate = minRate;
        this.maxRate = maxRate;
        
        setLayout(new BorderLayout());
        setSize(450, 125);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        
        component = createComponent();
        add(component, BorderLayout.NORTH);
        
        JLabel displayLabel = new JLabel(createTitle());
        JPanel labelPane = new JPanel();
        labelPane.add(displayLabel);
        super.add(labelPane, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    @Override
    public final void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        component.setVisible(aFlag);
    }

    public String createTitle() {
        return getMethodName().substring(3);
    }
    
    public abstract J createComponent();

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }
}
