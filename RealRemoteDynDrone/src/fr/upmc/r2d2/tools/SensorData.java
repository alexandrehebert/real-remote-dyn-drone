package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.annotations.BooleanSensorData;
import fr.upmc.dtgui.annotations.RealSensorData;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class SensorData/*<T/*, A extends Annotation>*/ extends HashMap.SimpleEntry<String, Object> {

    // private A a;
    
    public SensorData(String groupName, String methodName, Object value/*, A a*/) {
        super(methodName, value);
        //this.a = a;
    }
    
    /*public A getAnnotation() {
        return a;
    }*/
    
}

