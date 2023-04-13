package Display;

@FunctionalInterface
public interface Returnable {
    /**
     * This function must return the object that threw the exception to a known exception state
     */
    public void returnTo();
}
