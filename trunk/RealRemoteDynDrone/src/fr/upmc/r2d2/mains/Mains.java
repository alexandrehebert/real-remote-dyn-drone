package fr.upmc.r2d2.mains;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class Mains {
    
    // public static final String JAVASSIST = "fr.upmc.r2d2.Javassist";
    public static final String DEFAULT = "fr.upmc.r2d2.mains.MainWorld";
    public static final String ORIGINAL = "fr.upmc.dtgui.example.WorldTests";
    public static final String TESTS = "fr.upmc.r2d2.tests.MainTests";
    
    public static void main(String[] args) throws Throwable {
        MainJavassist.main(new String[] {TESTS});
    }
    
}
