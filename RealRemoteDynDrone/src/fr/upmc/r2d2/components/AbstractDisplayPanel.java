package fr.upmc.r2d2.components;

import javax.swing.JComponent;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractDisplayPanel<J extends JComponent> extends AbstractPanel<J> {

    public AbstractDisplayPanel() {
        super();
    }

    /**
     * On veut rafraichir le controle en fonction de la nouvelle valeur reçue
     * 
     * @param sd 
     */
    public abstract void update(Object sd);
    
}
