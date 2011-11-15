package fr.upmc.r2d2.components.controllers;

import fr.upmc.r2d2.components.AbstractControllerPanel;
import fr.upmc.r2d2.boards.AbstractTeleoperationBoard.ActuatorDataSender;
import fr.upmc.r2d2.components.JComponentFactory;
import fr.upmc.r2d2.robots.MessageData;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** 
 * Controller associé à un actuateur de type réel
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RealControllerPanel extends AbstractControllerPanel<JSlider> {
    
    /**
     * Informations des annotations
     */
    private String unit;
    private double minRange, maxRange;
    
    public RealControllerPanel(String groupName, String methodName, double minWritingRate, double maxWritingRate, String unit, double minRange, double maxRange) {
        super(groupName, methodName, minWritingRate, maxWritingRate);
        this.unit = unit;
        this.minRange = minRange;
        this.maxRange = maxRange;
        generateComponent();
    }
    
    /**
     * @return 
     */
    public String createTitle() {
        return super.createTitle() + " (" + unit + ")";
    }
    
    public JSlider createComponent() {
        return JComponentFactory.makeSlider(minRange, maxRange);
    }

    @Override
    public EventListener connect(final BlockingQueue bq) {
        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ActuatorDataSender.send(bq, new MessageData(getMethodName(), (double) component.getValue()));
            }
        };
        component.addChangeListener(cl);
        return cl;
    }

    @Override
    public void disconnect(EventListener el) {
        component.removeChangeListener((ChangeListener) el);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("unit=").append(unit).append(";");
        sb.append("range{min=").append(minRange).append(";");
        sb.append("max=").append(maxRange).append("}");
        return sb.toString();
    }
    
}
