package fr.upmc.r2d2.tools;

import fr.upmc.dtgui.robot.InstrumentedRobot;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class Utils {

    private static boolean verbose = true;

    /**
     * Debug
     * 
     * @param t 
     */
    public static void print(Throwable t) {
        if (!verbose) {
            return;
        }
        // System.out.println(t.getMessage());
        System.err.println(t.getMessage());
        t.printStackTrace(System.err);
    }

    /**
     * Affiche ou non les exceptions catchées pas l'assistant
     * 
     * @param verbose
     * @return 
     */
    public static void verbose(boolean verbose) {
        Utils.verbose = verbose;
    }

    public static String readSnippet(String name) {
        String file = "";
        try {
            InputStream ips = new FileInputStream("snippets/" + name + ".snippet");
            InputStreamReader ipsr = new InputStreamReader(ips);
            BufferedReader br = new BufferedReader(ipsr);
            String line;
            while ((line = br.readLine()) != null) {
                file += line;
            }
            br.close();
        } catch (Exception e) {
            Utils.print(e);
        }
        return file;
    }

    public static String serialize(Object o) {
        final StringBuffer s = new StringBuffer();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    s.append((char) b);
                }
            });
            oos.writeObject(o);
            oos.close();
        } catch (Exception e) {
        } finally {
            return s.toString();
        }
    }
    
    public static void block(String name, Block b) throws Exception {
        System.out.println("-------------------------------------------------");
        System.out.println("|\t" + whiteSpacesCompletion(name, 40) + "|");
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - -");
        b.run();
        System.out.println("-------------------------------------------------");
        System.out.println();
    }
    
    public static String whiteSpacesCompletion(String v, int size) {
        if (v.length() == size) return v;
        if (v.length() > size) return v.substring(0, size - 3) + "...";
        return whiteSpacesCompletion(v + " ", size);
    }
    
    public static interface Block /*extends Runnable*/ {
        void run() throws Exception;
    }
    
}