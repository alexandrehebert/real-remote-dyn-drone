package fr.upmc.r2d2.boards;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Réceptacle pour l'ensemble des contrôles rattachés à un groupe donné
 * Les contrôles seront ajoutés les uns à côté des autres
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class GroupPanel extends JPanel {

    protected int height = 0, width = 0;

    /**
     * @param name libellé du groupe
     */
    public GroupPanel(String name) {
        super.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        JLabel displayLabel = new JLabel(name);
        JPanel labelPane = new JPanel();
        labelPane.add(displayLabel);
        addComponent(labelPane);
        
        this.setVisible(true);
    }

    /**
     * Ajout d'un contrôle à la liste des contrôles existants du groupe
     * 
     * @param p composant à ajouter
     * @return 
     */
    protected final GroupPanel addComponent(JPanel p) {
        adjust(p);
        add(p);
        return this;
    }

    /**
     * Ajustement de la taille du composant à partir de la taille du nouveau
     * contrôle ajouté
     * 
     * @param p patron d'ajustement
     */
    private void adjust(JPanel p) {
        height += p.getHeight();
        width = (p.getWidth() > width) ? p.getWidth() : width;
    }

    /**
     * Application de la taille finale du composant
     */
    protected final void commit() {
        super.setSize(width, height);
    }
}
