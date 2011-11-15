package fr.upmc.r2d2.exceptions;

/**
 * Exception jetée par le Main principal si des arguments font défaut
 * 
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
public class UsageException extends Exception {
    public UsageException() {
        super("usage : java -jar me.java mainNumber");
    }
}
