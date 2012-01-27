package fr.upmc.r2d2.components.displays;

import fr.upmc.dtgui.annotations.VariationType;

/**
 * Display associé à un senseur de type entier
 * On hérite de RealDisplayPanel que l'on restreint au type entier
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class IntegerDisplayPanel extends RealDisplayPanel {
    
    public IntegerDisplayPanel(String groupName, String methodName, double minRate, double maxRate, String unit, int minRange, int maxRange, VariationType variation) {
        super(groupName, methodName, minRate, maxRate, unit, minRange, maxRange, variation);
    }
    
    @Override
    public void update(Object sd) {
        component.getModel().setValue((Integer)sd);
    }
    
}
