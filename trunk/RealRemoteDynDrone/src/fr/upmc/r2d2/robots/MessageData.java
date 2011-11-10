package fr.upmc.r2d2.robots;

import java.util.HashMap;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class MessageData extends HashMap.SimpleEntry<String, Object> {
    private String groupName = "";

    public MessageData(String methodName, Object value) {
        super(methodName, value);
    }
    
    public MessageData(String groupName, String methodName, Object value) {
        super(methodName, value);
        this.groupName = groupName;
    }    
    
    public String getGroupName() {
        return groupName;
    }
}

