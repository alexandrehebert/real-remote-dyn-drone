package fr.upmc.r2d2.components;

import java.awt.Color;
import javax.swing.JComponent;

/**
 * Classe abstraite de déclaration d'un contrôle de type display qui sera
 * gréffé dans un GroupPanel par la suite
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractDisplayPanel<J extends JComponent> extends AbstractPanel<J> {

    public AbstractDisplayPanel(String groupName, String methodName, double minRate, double maxRate) {
        super(groupName, methodName, minRate, maxRate);
    }

    public void generateComponent() {
        super.generateComponent();
        component.setEnabled(false);
        component.setBackground(Color.WHITE);
        component.validate();
    }
    
    /**
     * On veut rafraichir le controle en fonction de la nouvelle valeur reçue
     * 
     * @param sd 
     */
    public abstract void update(Object sd);
    
    public String toString() {
        return "display{" + super.toString() + "}";
    }
    
}
