package fr.upmc.r2d2.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractDisplayPanel extends JPanel {

    public AbstractDisplayPanel(String label) {
        super.setLayout(new BorderLayout());
        super.setSize(450, 125);
        JLabel displayLabel = new JLabel(label);
        JPanel labelPane = new JPanel();
        labelPane.add(displayLabel);
        super.add(display(), BorderLayout.NORTH);
        super.add(labelPane, BorderLayout.SOUTH);
        super.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
        super.setVisible(true);
    }

    /**
     * On souhaite créer le contrôle qui affichera la valeur
     * 
     * @return 
     */
    public abstract JPanel display();
    
    /**
     * On veut rafraichir le controle en fonction de la nouvelle valeur reçue
     * 
     * @param sd 
     */
    public abstract void update(SensorData sd);
    
}
