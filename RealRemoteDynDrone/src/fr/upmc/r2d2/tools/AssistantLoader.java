package fr.upmc.r2d2.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.Translator;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class AssistantLoader extends Loader {
    
    private ClassPool p = ClassPool.getDefault();
    private Map<Translator, List<String>> translators = new HashMap<Translator, List<String>>();
    private Map<String, CtClass> ctCache = new HashMap<String, CtClass>();
    
    public AssistantLoader() {
        super(ClassPool.getDefault());
        try {
            addTranslator(p, new DispatchTranslator());
        } catch (Exception e) {
            Utils.print(e);
        }
    }
    
    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        super.finalize();
        translators.clear();
    }
    
    /**
     * Ajoute un translator à la liste des translators
     * 
     * @param t translator à ajouter
     * @return 
     */
    public AssistantLoader addTranslator(ISimpleTranslator t) {
        return addTranslator(new TranslatorAdapter(t));
    }
    
    /**
     * Ajoute un translator de type simple à la liste des translators,
     * pour une ou plusieurs classes donnée(s). Ce translator sera averti
     * uniquement si la classe chargée est l'une des classes de la liste transmise
     * 
     * @param t translator à ajouter
     * @return 
     */
    public AssistantLoader addTranslator(ISimpleTranslator t, String... s) {
        return addTranslator(new TranslatorAdapter(t), s);
    }
        
    /**
     * Ajoute un translator natif à la liste des translators
     * 
     * @param t translator à ajouter
     * @return 
     */
    public AssistantLoader addTranslator(Translator t) {
        return addTranslator(t, new String[] {});
    }
        
    /**
     * Ajoute un translator de type simple à la liste des translators,
     * pour une ou plusieurs classes donnée(s). Ce translator sera averti
     * uniquement si la classe chargée est l'une des classes de la liste transmise
     * 
     * @param t translator à ajouter
     * @return 
     */
    public AssistantLoader addTranslator(Translator t, String... s) {
        translators.put(t, Arrays.asList(s));
        return this;
    }
    
    public void run(String main) {
        try {
            run(main, new String[] {});
        } catch (Throwable e) {
            Utils.print(e);
        }
    }

    
    /**
     * Interface de déclaration d'un translator de type simple
     * Ce translator est averti lors du chargement d'une classe
     */
    public static interface ISimpleTranslator {
        public void onLoad(ClassPool cp, String string, CtClass c) throws Exception;
    }
    
    /**
     * Ajoute le comportement onStart au [ISimpleTranslator]
     */
    public static interface IFullTranslator extends ISimpleTranslator {
        public void onStart(ClassPool cp) throws Exception;
    }
    
    /**
     * Classe wrapper qui permet de retirer du verbe ailleurs dans le code en
     * factorisant la déclaration des blocs try...catch et en allègeant le
     * comportement du Translator d'origine
     */
    private class TranslatorAdapter implements Translator {
        
        private ISimpleTranslator t;
        
        public TranslatorAdapter(ISimpleTranslator t) {
            this.t = t;
        }
        
        @Override
        public void start(ClassPool cp) throws NotFoundException, CannotCompileException {
            try {
                if (t instanceof IFullTranslator) ((IFullTranslator)t).onStart(cp);
            }
            catch (Throwable e) {
                Utils.print(e);
            }
        }
        @Override
        public void onLoad(ClassPool cp, String string) {
            try {
                t.onLoad(cp, string, getCtClass(cp, string));
            }
            catch (Throwable e) {
                Utils.print(e);
            }
            // finally {t.onLoad(cp, string, null);} // risque de recursion
        }
        
    }
    
    /**
     * Dispatche les évènements start et onLoad envoyés par javassist à tous les
     * translators ajoutés par le biais de addTranslator
     */
    private class DispatchTranslator implements Translator {

        @Override
        public void start(ClassPool cp) throws NotFoundException, CannotCompileException {
            for (Translator t : translators.keySet())
                t.start(cp);
        }
        
        @Override
        public void onLoad(ClassPool cp, String string) throws NotFoundException, CannotCompileException {
            for (Entry<Translator, List<String>> e : translators.entrySet())
                if (e.getValue().isEmpty() 
                        || e.getValue().contains(string)                // Si le nom de classe est associée au translator
                        || isImplement(cp, string, e.getValue())        // Si la classe implémente une des interfaces
                        || testJoker(string, e.getValue())              // Si la classe est contenue dans un certain package
                        || testAnnotations(cp, string, e.getValue()))   // Vérifie l'existance des annotations déclarées
                    e.getKey().onLoad(cp, string);
        }
        
        private boolean testJoker(String string, List<String> classzs) {
            for (String classz : classzs) {
                if (classz.equals(string))
                    return true;
                if (classz.endsWith("*"))
                    if (string.startsWith(classz.substring(0, classz.length()-1)))
                        return true;
            }
            return false;
        }
        
        private boolean isImplement(ClassPool cp, String string, List<String> classzs) throws NotFoundException {
            CtClass c = getCtClass(cp, string);
            
            if (!c.isInterface())
                for(CtClass inter : c.getInterfaces())
                    if (classzs.contains(inter.getName())) 
                        return true;

            return false;
        }
        
        private boolean testAnnotations(ClassPool cp, String string, List<String> classzs) {
            CtClass c = getCtClass(cp, string);
            
            try {
                for (Object annot : c.getAnnotations())
                    if (testJoker(annot.toString(), classzs))
                        return true;
            } catch (ClassNotFoundException ex) {
                Utils.print(ex);
            }

            return false;
        }
        
    }
    
    public CtClass getCtClass(ClassPool cp, String classz) {
        CtClass c = null;
        try {
            c = ((c = ctCache.get(classz)) != null) ? c : cp.get(classz);
        } catch (NotFoundException ex) {
            Utils.print(ex);
        }
        return c;
    }
    
    public CtMethod getCtMethod(CtClass cc, String methodz, CtClass... params) {
        try {
            return cc.getDeclaredMethod(methodz, params);
        }
        catch (Exception e) {
            Utils.print(e);
            return null;
        }
    }
    
}
