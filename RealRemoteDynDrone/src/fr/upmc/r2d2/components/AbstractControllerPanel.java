package fr.upmc.r2d2.components;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import javax.swing.JComponent;

/**
 * Classe abstraite de déclaration d'un contrôle de type controller qui sera
 * gréffé dans un GroupPanel par la suite
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractControllerPanel<J extends JComponent> extends AbstractPanel<J> {

    protected EventListener el;
    protected InstrumentedRobot r;

    public AbstractControllerPanel(String groupName, String methodName, double minRate, double maxRate) {
        super(groupName, methodName, minRate, maxRate);
    }
    
    public final void disconnectRobot() {
        this.r = null;
        disconnect(el);
    }

    public final void connectRobot(InstrumentedRobot r) {
        this.r = r;
        el = connect(r.getActuatorDataQueue());
    }

    /**
     * On veut connecter le contrôle à la queue d'envoi de commandes
     * 
     * @param bq
     * @return 
     */
    public abstract EventListener connect(final BlockingQueue bq);

    /**
     * On veut déconnecter le contrôle de la queue d'envoi de commandes
     * 
     * @param el 
     */
    public abstract void disconnect(EventListener el);
    
    public String toString() {
        return "controller{" + super.toString() + "}";
    }
    
}
