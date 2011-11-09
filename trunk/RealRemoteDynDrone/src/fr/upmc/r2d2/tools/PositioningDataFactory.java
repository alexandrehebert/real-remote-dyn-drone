package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.PositioningData;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class PositioningDataFactory {
    private final String labelx = "getX";
    private final String labely = "getY";
    private final String labeld = "getDirection";
    
    private static PositioningDataFactory uniq = new PositioningDataFactory();
    public static PositioningDataFactory getInstance() { return uniq; }

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
    
    private double x, y, d;
    private int stamp = 0;

    public PositioningData eat(MessageData data) {
       /*switch (PositioningDataAttribute.toAttrType(data.getKey())) {
            case X: 
            default:
                break;
        }*/
        
        switch (data.getKey()) {
            case labelx: x = (Integer) data.getValue();
                break;
            case labely: y = (Integer) data.getValue();
                break;
            case labeld: d = (Integer) data.getValue();
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
