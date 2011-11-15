package fr.upmc.r2d2.components.controllers;

import fr.upmc.r2d2.components.AbstractControllerPanel;
import fr.upmc.r2d2.boards.AbstractTeleoperationBoard.ActuatorDataSender;
import fr.upmc.r2d2.components.JComponentFactory;
import fr.upmc.r2d2.robots.MessageData;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class BooleanControllerPanel extends AbstractControllerPanel<JToggleButton> {
    
    public BooleanControllerPanel(String groupName, String methodName, double minWritingRate, double maxWritingRate) {
        super(groupName, methodName, minWritingRate, maxWritingRate);
        generateComponent();
    }
    
    public JToggleButton createComponent() {
        return JComponentFactory.makeToggleButton(false);
    }

    @Override
    public EventListener connect(final BlockingQueue bq) {
        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ActuatorDataSender.send(bq, new MessageData(getMethodName(), (boolean) component.isSelected()));
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
        return super.toString();
    }
    
}
