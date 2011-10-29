package fr.upmc.r2d2.tools;

import java.lang.annotation.Annotation;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class Actuator<T extends Annotation> {
    
    private String name;
    private T data;
    
    public Actuator(String name, T data) {
        this.name = name;
        this.data = data;
    }
    
    public String getName() {
        return name;
    }
    
    public T getData() {
        return data;
    }
    
}
