package enigma.machine;

import enigma.machine.component.code.Code;
import enigma.shared.dto.tracer.SignalTrace;

/**
 * Core Enigma machine API: configure code and process characters.
 * Implementations perform character processing using configured rotors
 * and reflector and must be deterministic.
 *
 * @since 1.0
 */
public interface Machine {
    /**
     * Apply a {@link Code} configuration to the machine.
     *
     * @param code code configuration containing rotors and reflector
     * @since 1.0
     */
    void setCode(Code code);

    /**
     * Process the provided character through the configured code.
     * Trace the signal propagation through the rotors and reflector.
     *
     * @param input input character
     * @return signal trace of the processing steps
     * @throws IllegalStateException when the machine has no configured code
     * @since 1.0
     */
    SignalTrace process(char input);

    /**
     * Check if the machine is configured with a code.
     *
     * @return true if the machine has a configured code; false otherwise
     * @since 1.0
     */
    boolean isConfigured();
}
