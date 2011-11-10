package fr.upmc.r2d2.components.controllers;

import fr.upmc.r2d2.components.AbstractControllerPanel;
import fr.upmc.r2d2.boards.AbstractTeleoperationBoard.ActuatorDataSender;
import fr.upmc.r2d2.components.JComponentFactory;
import fr.upmc.r2d2.tools.MessageData;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class RealControllerPanel extends AbstractControllerPanel<JSlider> {
 
    /**
     * Informations des annotations
     */
    private String groupName, methodName;
    private String unit;
    private int minRange, maxRange;
    private int minWritingRate, maxWritingRate;
    
    public RealControllerPanel(String label) {
        super();
    }

    /**
     * @TODO A générer avec Javassist
     * @return 
     */
    public String createTitle() {
        return "";
    }
    
    public JSlider createComponent() {
        return JComponentFactory.makeSlider(minRange, maxRange);
    } 

    @Override
    public EventListener connect(final BlockingQueue bq) {
        return new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();

                MessageData sc = new MessageData(methodName, source.getValue());
                        
                (new ActuatorDataSender(sc, bq)).start();
            }
        };
    }

    @Override
    public void disconnect(EventListener el) {
        component.removeChangeListener((ChangeListener) el);
    }
    
}
