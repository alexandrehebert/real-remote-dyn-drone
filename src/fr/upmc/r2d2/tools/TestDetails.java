package fr.upmc.r2d2.tools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDetails {
    
    int order() default 0;
    String description() default "Test";
    
}
