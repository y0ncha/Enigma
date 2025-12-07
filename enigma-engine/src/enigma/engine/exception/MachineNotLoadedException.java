package enigma.engine.exception;

/**
 * Exception thrown when attempting to use the machine before loading an XML specification.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>This exception is thrown when operations requiring a loaded machine specification
 * are attempted before calling {@code loadMachine(String path)}.</p>
 *
 * <p>The exception message includes guidance on how to load a machine.</p>
 *
 * @since 1.0
 */
public class MachineNotLoadedException extends EngineException {

    /**
     * Create an exception with the given message.
     *
     * @param message description of the error
     */
    public MachineNotLoadedException(String message) {
        super(message);
    }

    /**
     * Create an exception with the given message and cause.
     *
     * @param message description of the error
     * @param cause underlying exception
     */
    public MachineNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }
}
