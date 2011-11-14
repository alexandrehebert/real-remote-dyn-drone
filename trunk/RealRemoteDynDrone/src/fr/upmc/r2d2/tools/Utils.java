package fr.upmc.r2d2.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Utilitaires
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class Utils {

    private static boolean trace = true, logs = false;
    
    /**
     * Debug
     * 
     * @param t 
     */
    public static void print(Throwable t) {
        if (!trace) {
            return;
        }
        // System.out.println(t.getMessage());
        System.err.println(t);
        t.printStackTrace(System.err);
    }
    
    private static StringBuffer logger = new StringBuffer();
    
    public static void log(String l) {
        if (logger.length() == 0)
            Runtime.getRuntime().addShutdownHook(new Thread(){
                public void run() {
                    if (logs)
                        System.out.println(logger);
                }
            });
        logger.append(l).append("\n");
    }

    /**
     * Affiche ou non les exceptions catchées pas l'assistant
     * 
     * @param verbose
     * @return 
     */
    public static void trace(boolean verbose) {
        Utils.trace = trace;
    }
    
    public static void log(boolean verbose) {
        Utils.logs = verbose;
    }

    /**
     * Récupère le contenu d'un snippet dans une chaine de caractères
     * 
     * @param name
     * @return 
     */
    public static String readSnippet(String name) {
        String file = "";
        try {
            InputStream ips = new FileInputStream("snippets/" + name + ".snippet");
            InputStreamReader ipsr = new InputStreamReader(ips);
            try (BufferedReader br = new BufferedReader(ipsr)) {
                String line = "";
                while ((file += line) != null && (line = br.readLine()) != null /** @TODO heyhey */) {}
            }
        } catch (Exception e) {
            Utils.print(e);
        }
        return file;
    }

    /**
     * Récupère le résultat de la sérialisation d'un objet dans une chaine
     * de caractères
     * 
     * @param o
     * @return 
     */
    public static String serialize(Object o) {
        final StringBuffer s = new StringBuffer();
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new OutputStream() {
                     @Override
                     public void write(int b) throws IOException {
                         s.append((char) b);
                     }
                 })) {
                oos.writeObject(o);
            }
        } catch (Exception e) {
        } finally {
            return s.toString();
        }
    }
    
    /**
     * Affiche un block dans le terminal pour une meilleure lisibilité du debug
     * 
     * @param name
     * @param b
     * @throws Exception 
     */
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
    
    
    /**
     * Convertit les classes wrappers (Double, Integer etc...) en classe de type 
     * primitifs (double, int etc.)
     * @param c
     * @return 
     */
    public static Class class2primitive(Class c) {
        if (c == Double.class)
            return double.class;
        if (c == Integer.class)
            return int.class;
        if (c == Boolean.class)
            return boolean.class;
        if (c == Float.class)
            return float.class;
        if (c == Byte.class)
            return byte.class;
        return c;
    }    
}