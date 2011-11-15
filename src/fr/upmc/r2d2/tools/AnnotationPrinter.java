package fr.upmc.r2d2.tools;

import java.lang.annotation.Annotation;

/**
 * Classe d'affichage correct des annotations
 * pour palier au défaut du toString dans les annotations d'origine
 * Ainsi, on ne modifie pas le code d'origine mais on gagne en flexibilité au 
 * niveau de Javassist
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class AnnotationPrinter {
    
    public static enum AnnotationType {
        ACTUATOR,
        SENSOR
    }
    
    private Annotation a;
    
    public AnnotationPrinter(Annotation a) {
        this.a = a;
    }
    
    public AnnotationType getType() {
        return (a.annotationType().getName().endsWith("ActuatorData") ? AnnotationType.ACTUATOR : AnnotationType.SENSOR);
    }
    
    public String getVarType() {
        String s = "";
        switch (getType()) {
            case ACTUATOR: s = a.annotationType().getSimpleName().split("ActuatorData")[0]; break;
            case SENSOR: s = a.annotationType().getSimpleName().split("SensorData")[0]; break;
        }
        return s.toLowerCase();
    }
    
    @Override
    public String toString() {
        return getType().toString().toLowerCase() + " " + getVarType();
    }
    
}
