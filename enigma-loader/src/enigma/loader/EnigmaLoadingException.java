package enigma.loader;

/**
 * Exception thrown when an error occurs during XML loading/validation.
 *
 * <p><b>Module:</b> enigma-loader</p>
 *
 * <p>This exception represents failures in the loading and validation process,
 * including:</p>
 * <ul>
 *   <li>File not found or invalid extension</li>
 *   <li>XML parsing errors (malformed XML)</li>
 *   <li>Validation failures (alphabet, rotor, reflector constraints)</li>
 * </ul>
 *
 * <p>Exception messages are detailed and indicate the specific validation
 * rule that failed.</p>
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
