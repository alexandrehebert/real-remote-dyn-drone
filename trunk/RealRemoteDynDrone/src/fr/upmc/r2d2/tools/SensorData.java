package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.annotations.BooleanSensorData;
import fr.upmc.dtgui.annotations.RealSensorData;
import java.lang.annotation.Annotation;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class SensorData<T, A extends Annotation> {
    
    private T value;
    private A a;
    
    public SensorData(T value, A a) {
        this.value = value;
        this.a = a;
    }
    
    public T getValue() {
        return value;
    }
    
    public A getAnnotation() {
        return a;
    }
    
    public static class RealSensorCapsule extends SensorData<Double, RealSensorData> {

        public RealSensorCapsule(Double v, RealSensorData d) {
            super(v, d);
        }

    }
    
    public static class BooleanSensorCapsule extends SensorData<Boolean, BooleanSensorData> {

        public BooleanSensorCapsule(Boolean v, BooleanSensorData d) {
            super(v, d);
        }

    }
    
}

