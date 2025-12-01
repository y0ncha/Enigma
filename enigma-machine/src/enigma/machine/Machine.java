package enigma.machine;

import enigma.machine.code.Code;
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
     * Process a single character through the configured machine.
     *
     * @param input input character
     * @return processed output character
     * @throws IllegalStateException when the machine has no configured code
     * @since 1.0
     */
    char process(char input);

    /**
     * Process a single character and produce a detailed {@link SignalTrace}.
     * The returned {@link SignalTrace} contains a complete, deterministic
     * description of this character's processing path: rotor steps, per-rotor
     * forward/backward traces and reflector activity. Implementations should
     * throw {@link IllegalStateException} when the machine is not properly
     * configured (for example, missing code or keyboard) rather than returning
     * a partial trace.
     *
     * @param input input character to trace
     * @return a {@link SignalTrace} describing the processing of the input
     * @throws IllegalStateException if the machine is not configured with a code or keyboard
     * @since 1.0
     */
    SignalTrace processDebug(char input);

    boolean isConfigured();
}
