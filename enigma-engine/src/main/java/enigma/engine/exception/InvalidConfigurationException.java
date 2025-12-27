package enigma.engine.exception;

/**
 * Exception thrown when machine configuration is invalid.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>This exception represents failures in code configuration, including:</p>
 * <ul>
 *   <li>Invalid rotor IDs (non-existent or duplicate)</li>
 *   <li>Invalid rotor count (not matching machine specification)</li>
 *   <li>Invalid reflector ID (non-existent)</li>
 *   <li>Invalid initial positions (not in alphabet)</li>
 *   <li>Invalid plugStr configuration</li>
 * </ul>
 *
 * <p>Exception messages include the specific parameter that failed validation
 * and guidance on how to fix the issue.</p>
 *
 * @since 1.0
 */
public class InvalidConfigurationException extends EngineException {

    /**
     * Create an exception with the given message.
     *
     * @param message description of the configuration error
     */
    public InvalidConfigurationException(String message) {
        super(message);
    }

    /**
     * Create an exception with the given message and cause.
     *
     * @param message description of the configuration error
     * @param cause underlying exception
     */
    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
