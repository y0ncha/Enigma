package enigma.engine.components.loader;

public class EnigmaLoadingException extends Exception {

    public EnigmaLoadingException(String message) {
        super(message);
    }

    public EnigmaLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
