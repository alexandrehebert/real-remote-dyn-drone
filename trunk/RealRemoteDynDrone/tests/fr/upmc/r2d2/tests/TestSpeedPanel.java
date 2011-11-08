package fr.upmc.r2d2.tests;

import fr.upmc.r2d2.tools.AbstractControllerPanel;
import fr.upmc.r2d2.tools.AbstractDisplayPanel;
import fr.upmc.r2d2.tools.AbstractPanel;
import fr.upmc.r2d2.tools.SensorData;
import java.awt.FlowLayout;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class TestSpeedPanel extends AbstractPanel {

    private JSlider slider;
    private DefaultBoundedRangeModel model;

    public TestSpeedPanel() {
        super(450, 250);
        setController(new AbstractControllerPanel() {

            @Override
            public EventListener connect(BlockingQueue bq) {
                SpeedActuatorDataListener el = new SpeedActuatorDataListener(bq);
                slider.addChangeListener(el);
                return el;
            }

            @Override
            public void disconnect(EventListener el) {
                slider.removeChangeListener((SpeedActuatorDataListener) el);
            }
        });
        setDisplay(new AbstractDisplayPanel("Speed control (m/s)") {

            @Override
            public JPanel display() {
                JPanel jpProgressBar = new JPanel();
                jpProgressBar.setLayout(new FlowLayout());
                model = new DefaultBoundedRangeModel(0, 0, 0, 20);
                slider = new JSlider(model);
                slider.setMajorTickSpacing(5);
                slider.setMinorTickSpacing(1);
                slider.setPaintTicks(true);
                slider.setPaintLabels(true);
                jpProgressBar.add(slider);
                return jpProgressBar;
            }

            @Override
            public void update(SensorData sd) {
                model.setValue((int) Math.round((Integer) sd.getValue()));
            }
        });
        commit();
    }
}
