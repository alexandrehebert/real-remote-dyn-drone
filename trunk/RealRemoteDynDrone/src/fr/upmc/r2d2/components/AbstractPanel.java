package fr.upmc.r2d2.components;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Element constitutif d'un GroupPanel
 * Peut être soit un controller, soit un display
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public abstract class AbstractPanel<J extends JComponent> extends JPanel {
    
    protected J component;
    private String groupName;
    private String methodName;
    protected double minRate, maxRate;    
    
    public AbstractPanel(String groupName, String methodName, double minRate, double maxRate) {
        super();
        
        this.groupName = groupName;
        this.methodName = methodName;
        this.minRate = minRate;
        this.maxRate = maxRate;
        
        setLayout(new BorderLayout());
        setSize(450, 125);
        setBorder(BorderFactory.createLineBorder(Color.ORANGE, 1));
    }
    
    /**
     * Génère le controle associé au Panel et l'ajou à ce dernier
     */
    public void generateComponent() {
        component = createComponent();
        add(component, BorderLayout.NORTH);
        
        JLabel displayLabel = new JLabel(createTitle());
        JPanel labelPane = new JPanel();
        labelPane.add(displayLabel);
        super.add(labelPane, BorderLayout.SOUTH);
        
        setVisible(true);
        validate();
    }
    
    @Override
    public final void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        component.setVisible(aFlag);
    }

    /**
     * Par défaut le titre du controle est le nom de la méthode associée à l'actuateur
     * ou au senseur privé de "set" ou "get"
     * 
     * @return 
     */
    public String createTitle() {
        return getMethodName().substring(3);
    }
    
    /**
     * Créé le controle associé au panel, diffère en fonction des displays et des
     * controllers ainsi que des types Real, Integer ou Boolean des données qu'ils
     * permettent de manipuler (actuators, sensors)
     * 
     * @return 
     */
    public abstract J createComponent();

    /**
     * @return the groupName
     */
    public final String getGroupName() {
        return groupName;
    }

    /**
     * @return the methodName
     */
    public final String getMethodName() {
        return methodName;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("method=").append(getMethodName()).append(";");
        sb.append("group=").append(getGroupName()).append(";");
        sb.append("rate{min=").append(minRate).append(";");
        sb.append("max=").append(maxRate).append("};");
        return sb.toString();
    }
    
}
