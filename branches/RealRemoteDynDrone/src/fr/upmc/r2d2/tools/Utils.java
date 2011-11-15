package fr.upmc.r2d2.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Utilitaires
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class Utils {

    private static boolean trace = true, logs = false;
    public static final int DEBUG_WIDTH = 71;
    
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
    
    public static void print(List<?> list) {
        if (list.isEmpty()) print("[]");
        StringBuilder sb = new StringBuilder("[" + list.get(0));
        for (Object o : list.subList(1, list.size()))
            sb.append(", ").append(o);
        System.out.println(sb + "]");
    }
    
    public static void print(String s) {
        Block.print(s);
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
                // qui a dit qu'on ne pouvait pas lire un fichier en une seule ligne de java ? :D
                while ((file += line) != null && (line = br.readLine()) != null) {}
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
    public static void block(String name, Utils.Block b) throws Exception {
        System.out.println();
        System.out.println(charCompletion("/", "=") + "\\");
        System.out.println(whiteSpacesCompletion("|    " + name) + "|");
        b.line();
        System.out.println();
        b.run();
        System.out.println();
        System.out.println(charCompletion("\\", "=") + "/");
        System.out.println();
    }
    
    public static String whiteSpacesCompletion(String v) {
        return charCompletion(v, " ", DEBUG_WIDTH);
    }
    
    public static String charCompletion(String v, String c) {
        return charCompletion(v, c, DEBUG_WIDTH);
    }
    
    public static String whiteSpacesCompletion(String v, int size) {
        return charCompletion(v, " ", size);
    }
    
    public static String charCompletion(String v, String c, int size) {
        if (v.length() == size) return v;
        if (v.length() > size) return v.substring(0, size - 3) + "...";
        return charCompletion(v + c, c, size);
    }
    
    /**
     * Classe d'affichage esthétique des debugs et autres
     * En définissant la méthode run, on admet que l'ensemble des affichages produit
     * à l'intérieur de son appel seront encapsulés dans un joli petit bloc bien
     * indenté
     */
    public static abstract class Block /*extends Runnable*/ {
        
        private String label = "";
        private int errs = 0;
        
        abstract public void run() throws Exception;
        
        public void line() {
            System.out.println(charCompletion("+", "-") + "+");
        }
        
        public static void print() {
            System.out.println();
        }
        
        public static void flush() {
            System.out.flush();
            System.err.flush();
        }
        
        public static void print(String s) {
            //System.out.println(whiteSpacesCompletion("| " + s, DEBUG_WIDTH - 1) + " |");
            System.out.println(/*"| " + */s);
        }
        
        public static void state(String state) {
            state(state, System.out);
        }
        
        public static void state(String state, PrintStream stream) {
            stream.println(Utils.whiteSpacesCompletion("[ " + state, DEBUG_WIDTH-1) + " ]");
        }
        
        public void go(String label, String more) {
            this.label = label;
            state("RUN " + label + " - " + more);
        }
        
        public void go(String label) {
            this.label = label;
            state("RUN " + label);
        }
        
        public void pass() {
            state("PASS " + label);
        }
        
        public void err(Throwable t) {
            err(t.getCause() + "");
            t.printStackTrace();
        }
        
        public void err(String s) {
            flush();
            state("ERR" + (++errs) + " " + s, System.err);
        }
        
        public int errors() {
            return errs;
        }
    }
    
    
    /**
     * Convertit les classes wrappers (Double, Integer etc...) en classe de type 
     * primitifs (double, int etc.)
     * 
     * @param c classe wrapper
     * @return classe primitive
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