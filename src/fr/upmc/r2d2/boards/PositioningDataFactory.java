package fr.upmc.r2d2.boards;

import fr.upmc.dtgui.robot.PositioningData;
import fr.upmc.r2d2.robots.MessageData;

/**
 * Construction d'un PositioningData à partir des données envoyées par les
 * sensors du robot
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class PositioningDataFactory {
    
    private final String labelx = "getX";
    private final String labely = "getY";
    private final String labeld = "getDirection";
    
    /**
     * @return singleton
     */
    public static PositioningDataFactory getInstance() { return uniq; }
    private PositioningDataFactory() {}
    private static PositioningDataFactory uniq = new PositioningDataFactory();

    /*public static enum PositioningDataAttribute {

        X, Y, DIRECTION, EX;

        public static PositioningDataAttribute toAttrType(String methodName) {
            for (PositioningDataAttribute p : PositioningDataAttribute.values()) {
                if (methodName.toUpperCase().endsWith(p.toString())) {
                    return p;
                }
            }
            return PositioningDataAttribute.EX;
        }
        
    }*/
    
    private Double x, y, d;
    private int stamp = 0;

    /**
     * L'objectif ici est d'avaler un certain nombre de MessageData jusqu'à obtenir
     * l'ensemble des données nécessaires à la construction d'une instance de type
     * PositioningData, qui nous sera indispensable pour l'affichage du robot 
     * dans l'univers
     * 
     * @param data donnée "mangée"
     * @return 
     */
    public PositioningData eat(MessageData data) {
        if (!data.getGroupName().equals("position")) return null;
       /*switch (PositioningDataAttribute.toAttrType(data.getKey())) {
            case X: 
            default:
                break;
        }*/
        
        switch (data.getKey()) {
            case labelx: x = (Double) data.getValue();
                break;
            case labely: y = (Double) data.getValue();
                break;
            case labeld: d = (Double) data.getValue();
                break;    
            default: return null; /* erreur :-) */
        }
        
        if (++stamp == 3) {
            stamp = 0;
            return new PositioningData(x, y, d);
        }
        
        return null;
    }
    
}
