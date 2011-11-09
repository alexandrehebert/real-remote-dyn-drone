package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.annotations.VariationType;
import javax.swing.JSlider;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RealDisplayPanel extends AbstractDisplayPanel<JSlider> {
     
    /**
     * Informations des annotations
     */
    private String groupName, methodName;
    private String unit;
    private int minRange, maxRange;
    private int minReadingRate, maxReadingRate;
    private VariationType variation;
    
    public RealDisplayPanel() {
        super();
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
     * @TODO A machiner dans javassist
     * @return 
     */
    @Override
    public String createTitle() {
        return "";
    }
    
}
