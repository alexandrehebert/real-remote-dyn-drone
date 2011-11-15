package fr.upmc.r2d2.robots;

import java.util.HashMap;

/**
 * Type de donnée transitant entre les boards et les robots
 * Cette "capsule" est suffisament générique pour supporter n'importe quel
 * type de donnée
 * 
 * <strong>On s'abstrait ainsi du mécanisme de plus bas niveau qui consistait à construire
 * une capsule différente par actuateur et par senseur</strong>
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MessageData extends HashMap.SimpleEntry<String, Object> {
    
    private String groupName = "";
    
    /**
     * 
     * @param methodName nom de la méthode (du robot) qui sera appelée par une commande
     * @param value valeur qui sera transmise à cette méthode s'il s'agit d'un actuateur
     */
    public MessageData(String methodName, Object value) {
        super(methodName, value);
    }
    
    public MessageData(String groupName, String methodName, Object value) {
        this(methodName, value);
        this.groupName = groupName;
    }
    
    /**
     * Nom du groupe
     * 
     * @return groupName
     */
    public String getGroupName() {
        return groupName;
    }
    
    public String toString() {
        return "md{g=" + groupName + ";m=" + getKey() + ";v=" + getValue() + "}";
    }
    
}
