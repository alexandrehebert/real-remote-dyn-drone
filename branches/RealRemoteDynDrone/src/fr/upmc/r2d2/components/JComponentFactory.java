package fr.upmc.r2d2.components;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Utilitaire de fabrication des composants
 * On factorise ainsi le code de génération des sliders qui auront le même look
 * and feel quel que soit la situation
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class JComponentFactory {
    
    public static final String TBUTTON_ON = "On";
    public static final String TBUTTON_OFF = "Off";
    
    /**
     * Génère un JSlider
     * 
     * @param minRange taux de rafraichissement minimum
     * @param maxRange taux de fafraichissement maximum
     * @return JSlider généré
     */
    public static JSlider makeSlider(double minRange, double maxRange) {
        DefaultBoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, (int) minRange, (int) maxRange);   
        JSlider js = new JSlider(model);
        
        double range = (maxRange - minRange);
        range = Math.ceil(range / 2.);
        
        js.setOrientation(JSlider.VERTICAL);
        js.setMajorTickSpacing((int) range);
        js.setMinorTickSpacing((int) (range / 2));
        js.setPaintTicks(true);
        js.setPaintLabels(true);
        
        return js;
    }
    
    /**
     * Génère un JToggleButton
     * 
     * @param state état par défaut du bouton
     * @return JToggleButton généré
     */
    public static JToggleButton makeToggleButton(boolean state) {
        return makeToggleButton(TBUTTON_ON, TBUTTON_OFF, state);
    }
    
    public static JToggleButton makeToggleButton(String label, boolean state) {
        return new JToggleButton(label, state);
    }
    
    public static JToggleButton makeToggleButton(final String labelTrue, final String labelFalse, final boolean state) {
        final JToggleButton jtp = makeToggleButton(state ? labelTrue : labelFalse, state);
        jtp.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                jtp.setText(jtp.isSelected() ? labelTrue : labelFalse);
            }
        });
        return jtp;
    }
    
    /**
     * Génère un JProgressBar
     * 
     * @param minRange taux de rafraichissement minimum
     * @param maxRange taux de fafraichissement maximum
     * @return JProgressBar généré
     */
    public static JProgressBar makeProgressBar(double minRange, double maxRange) {
        return makeProgressBar(minRange, maxRange, 0);
    }
    
    public static JProgressBar makeProgressBar(double minRange, double maxRange, double value) {
        JProgressBar jpb = new JProgressBar((int)minRange, (int)maxRange);
        jpb.setValue((int)value);
        return jpb;
    }
    
}
