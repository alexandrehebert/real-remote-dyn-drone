package fr.upmc.r2d2.components.displays;

import fr.upmc.dtgui.annotations.VariationType;
import fr.upmc.r2d2.components.AbstractDisplayPanel;
import fr.upmc.r2d2.components.JComponentFactory;
import javax.swing.JSlider;

/**
 * Display associé à un senseur de type réel
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RealDisplayPanel extends AbstractDisplayPanel<JSlider> {
     
    /**
     * Informations des annotations
     */
    private String unit;
    private double minRange, maxRange;
    private VariationType variation;
    
    public RealDisplayPanel(String groupName, String methodName, double minRate, double maxRate, String unit, double minRange, double maxRange, VariationType variation) {
        super(groupName, methodName, minRate, maxRate);
        this.unit = unit;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.variation = variation;
        generateComponent();
    }
    
    @Override
    public void update(Object sd) {
        component.getModel().setValue((int) Math.round((Double)sd));
    }
    
    /**
     * @TODO générer progressbar ou slider en fonction de variationType
     * @return 
     */
    @Override
    public JSlider createComponent() {
        return JComponentFactory.makeSlider(minRange, maxRange);
    }

    /**
     * @return 
     */
    @Override
    public String createTitle() {
        return super.createTitle() + " (" + unit + ")";
    }
    
}
