package enigma.engine.exception;

/**
 * Base exception for all engine-related errors.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>This is the parent class for all domain-specific exceptions thrown
 * by the engine during machine configuration, validation, and message processing.</p>
 *
 * <p>All exception messages follow the pattern:</p>
 * <ul>
 *   <li><b>What</b> is wrong</li>
 *   <li><b>Where</b> the problem occurred (rotor ID, reflector ID, position index, etc.)</li>
 *   <li><b>How</b> to fix it</li>
 * </ul>
 *
 * @since 1.0
 */
public class EngineException extends RuntimeException {

    /**
     * Create an exception with the given message.
     *
     * @param message description of the error
     */
    public EngineException(String message) {
        super(message);
    }

    /**
     * Create an exception with the given message and cause.
     *
     * @param message description of the error
     * @param cause underlying exception
     */
    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
