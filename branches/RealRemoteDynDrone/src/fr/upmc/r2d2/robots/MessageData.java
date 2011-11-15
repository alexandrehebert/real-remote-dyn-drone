package fr.upmc.r2d2.robots;

import java.util.HashMap;

/**
 * Type de donnée transitant entre les boards et les robots
 * Cette "capsule" est suffisament générique pour supporter n'importe quel
 * type de donnée
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MessageData extends HashMap.SimpleEntry<String, Object> {
    private String groupName = "";

    public MessageData(String methodName, Object value) {
        super(methodName, value);
    }
    
    public MessageData(String groupName, String methodName, Object value) {
        this(methodName, value);
        this.groupName = groupName;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public String toString() {
        return "md{g=" + groupName + ";m=" + getKey() + ";v=" + getValue() + "}";
    }
}
