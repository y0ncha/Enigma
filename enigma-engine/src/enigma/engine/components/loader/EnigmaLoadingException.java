package enigma.engine.components.loader;

/**
 * Exception thrown when an error occurs during the loading process in the Enigma engine.
 * <p>
 * This exception represents failures related to loading components, resources, or configuration
 * within the Enigma engine. It is typically thrown when the loading process encounters
 * invalid data, missing files, or other unrecoverable errors.
 */
public class EnigmaLoadingException extends Exception {

    public EnigmaLoadingException(String message) {
        super(message);
    }

    public EnigmaLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
