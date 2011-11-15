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

    /**
     * Si le controller est connecté à un robot, el contient le listener associé au contrôle
     */
    protected EventListener el;
    
    /**
     * Si le controller est connecté à un robot, r != null
     */
    protected InstrumentedRobot r;

    public AbstractControllerPanel(String groupName, String methodName, double minRate, double maxRate) {
        super(groupName, methodName, minRate, maxRate);
    }
    
    /**
     * Déconnecte le robot du controller
     * Le listener courant est supprimé du contrôle et r est mis à null
     */
    public final void disconnectRobot() {
        this.r = null;
        disconnect(el);
    }

    /**
     * On récupère la référence sur le robot à controler et on connecte le contrôle
     * aux évènements clients pour envoyer des commandes au robot si besoin
     * 
     * @param r robot que l'on désire connecter au controller
     */
    public final void connectRobot(InstrumentedRobot r) {
        this.r = r;
        el = connect(r.getActuatorDataQueue());
    }

    /**
     * On veut connecter le contrôle à la queue d'envoi de commandes
     * 
     * @param bq queue de reception de messages du robot
     * @return listener rattaché au contrôle
     */
    public abstract EventListener connect(final BlockingQueue bq);

    /**
     * On veut déconnecter le contrôle de la queue d'envoi de commandes
     * 
     * @param el listener rattaché au contrôle
     */
    public abstract void disconnect(EventListener el);
    
    public String toString() {
        return "controller{" + super.toString() + "}";
    }
    
}
