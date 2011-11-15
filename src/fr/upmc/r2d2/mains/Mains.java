package fr.upmc.r2d2.mains;

import fr.upmc.r2d2.exceptions.UsageException;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class Mains {
    
    // public static final String JAVASSIST = "fr.upmc.r2d2.Javassist";
    public static final String DEFAULT = "fr.upmc.r2d2.mains.MainWorld";
    public static final String ORIGINAL = "fr.upmc.dtgui.example.WorldTests";
    public static final String TESTS = "fr.upmc.r2d2.tests.MainTests";
    public static final String[] MAINS = new String[] {DEFAULT, ORIGINAL, TESTS};
    
    /**
     * Selection du main à lancer au démarrage
     * 
     * @param args
     * @throws Throwable 
     */
    public static void main(String[] args) throws Throwable {
        if (args.length != 1) {
            System.out.println("mains :");
            int c = 0;
            for (String s : MAINS) System.out.println(++c + "/ " + s);
            throw new UsageException();
        }
        try {
            int m = Integer.parseInt(args[0]);
            if (m < 0 || m >= MAINS.length) m = 0;
            MainJavassist.main(new String[] {MAINS[m]});
        }
        catch (Exception e) {
            System.out.println("invalid argument");
            throw new UsageException();
        }
    }
    
}
