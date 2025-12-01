package enigma.engine.loader;

/**
 * Exception thrown when an error occurs during the loading process in the Enigma engine.
 * <p>
 * This exception represents failures related to loading components, resources, or configuration
 * within the Enigma engine. It is typically thrown when the loading process encounters
 * invalid data, missing files, or other unrecoverable errors.
 *
 * @since 1.0
 */
public class EnigmaLoadingException extends Exception {

    /**
     * Create an exception with the given message.
     *
     * @param message description of the loading failure
     */
    public EnigmaLoadingException(String message) {
        super(message);
    }

    /**
     * Create an exception with the given message and cause.
     *
     * @param message description of the loading failure
     * @param cause underlying exception
     */
    public EnigmaLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
