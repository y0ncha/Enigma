package enigma.engine.exception;

/**
 * Exception thrown when input message contains invalid characters.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>This exception represents failures in message validation, including:</p>
 * <ul>
 *   <li>Characters not in the machine alphabet</li>
 *   <li>Forbidden control characters (newline, tab, ESC, etc.)</li>
 * </ul>
 *
 * <p>Exception messages include the invalid character, its position in the input,
 * and the valid alphabet for the current machine configuration.</p>
 *
 * @since 1.0
 */
public class InvalidMessageException extends EngineException {

    /**
     * Create an exception with the given message.
     *
     * @param message description of the input error
     */
    public InvalidMessageException(String message) {
        super(message);
    }

    /**
     * Create an exception with the given message and cause.
     *
     * @param message description of the input error
     * @param cause underlying exception
     */
    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
