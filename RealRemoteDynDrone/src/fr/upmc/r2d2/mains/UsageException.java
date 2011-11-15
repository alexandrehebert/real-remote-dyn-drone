package fr.upmc.r2d2.mains;

/**
 * @author Alexandre Hebert
 * @author Thomas Champion
 */
class UsageException extends Exception {
    public UsageException() {
        super("usage : java -jar me.java mainNumber");
    }
}
