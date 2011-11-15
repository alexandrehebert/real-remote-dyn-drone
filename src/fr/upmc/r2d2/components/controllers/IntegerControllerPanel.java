package fr.upmc.r2d2.components.controllers;

import fr.upmc.r2d2.boards.AbstractTeleoperationBoard.ActuatorDataSender;
import fr.upmc.r2d2.robots.MessageData;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class IntegerControllerPanel extends RealControllerPanel {
    
    public IntegerControllerPanel(String groupName, String methodName, double minWritingRate, double maxWritingRate, String unit, int minRange, int maxRange) {
        super(groupName, methodName, minWritingRate, maxWritingRate, unit, minRange, maxRange);
    }
    
    @Override
    public EventListener connect(final BlockingQueue bq) {
        ChangeListener cl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                ActuatorDataSender.send(bq, new MessageData(getMethodName(), (int) component.getValue()));
            }
        };
        component.addChangeListener(cl);
        return cl;
    }
    
}
