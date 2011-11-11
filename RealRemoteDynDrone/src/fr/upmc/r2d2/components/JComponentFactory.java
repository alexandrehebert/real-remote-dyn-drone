package fr.upmc.r2d2.components;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class JComponentFactory {
    
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
    
}
