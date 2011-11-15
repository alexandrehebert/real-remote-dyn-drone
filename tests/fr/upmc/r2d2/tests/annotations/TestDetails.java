package fr.upmc.r2d2.tests.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation nous permettant d'ordonnancer les différents tests et de donner
 * quelques précisions lors de l'execution dudit test annoté
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface TestDetails {
    
    int order() default 0;
    String description() default "Test";
    
}
