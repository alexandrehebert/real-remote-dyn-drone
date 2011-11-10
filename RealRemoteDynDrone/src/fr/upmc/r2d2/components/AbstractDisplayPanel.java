package fr.upmc.r2d2.components;

import javax.swing.JComponent;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractDisplayPanel<J extends JComponent> extends AbstractPanel<J> {

    public AbstractDisplayPanel(String groupName, String methodName, double minRate, double maxRate) {
        super(groupName, methodName, minRate, maxRate);
    }

    /**
     * On veut rafraichir le controle en fonction de la nouvelle valeur reçue
     * 
     * @param sd 
     */
    public abstract void update(Object sd);
    
}
