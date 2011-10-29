package fr.upmc.r2d2.tools;

import java.lang.annotation.Annotation;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class SensorData {
    
    private Object value;
    private Annotation a;
    
    public SensorData(Object value, Annotation a) {
        this.value = value;
        this.a = a;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Annotation getAnnotation() {
        return a;
    }
    
}
