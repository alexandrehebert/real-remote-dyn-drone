package fr.upmc.r2d2.components.displays;

import fr.upmc.r2d2.components.AbstractDisplayPanel;
import fr.upmc.r2d2.components.JComponentFactory;
import javax.swing.JToggleButton;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class BooleanDisplayPanel extends AbstractDisplayPanel<JToggleButton> {
    
    public BooleanDisplayPanel(String groupName, String methodName, double minRate, double maxRate) {
        super(groupName, methodName, minRate, maxRate);
        generateComponent();
    }
    
    @Override
    public void update(Object sd) {
        component.setSelected((Boolean)sd);
    }
    
    /**
     * @TODO générer progressbar ou slider en fonction de variationType
     * @return 
     */
    @Override
    public JToggleButton createComponent() {
        return JComponentFactory.makeToggleButton(false);
    }
    
}
