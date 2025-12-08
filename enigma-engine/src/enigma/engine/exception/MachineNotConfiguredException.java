package enigma.engine.exception;

/**
 * Exception thrown when attempting to process messages before configuring the machine.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>This exception is thrown when operations requiring a configured machine
 * are attempted before calling {@code configManual(CodeConfig)} or {@code configRandom()}.</p>
 *
 * <p>The exception message includes guidance on how to configure the machine.</p>
 *
 * @since 1.0
 */
public class MachineNotConfiguredException extends EngineException {

    /**
     * Create an exception with the given message.
     *
     * @param message description of the error
     */
    public MachineNotConfiguredException(String message) {
        super(message);
    }

    /**
     * Create an exception with the given message and cause.
     *
     * @param message description of the error
     * @param cause underlying exception
     */
    public MachineNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }
}
