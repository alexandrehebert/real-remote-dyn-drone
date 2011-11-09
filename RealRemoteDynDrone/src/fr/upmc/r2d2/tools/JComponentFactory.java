package fr.upmc.r2d2.tools;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class JComponentFactory {
    
    public static JSlider makeSlider(int minRange, int maxRange) {
        DefaultBoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, minRange, maxRange);   
        JSlider js = new JSlider(model);
        
        js.setOrientation(JSlider.VERTICAL);
        js.setMajorTickSpacing(20);
        js.setMinorTickSpacing(5);
        js.setPaintTicks(true);
        js.setPaintLabels(true);  
        
        return js;
    }
    
}
