package enigma.machine;

import enigma.machine.component.code.Code;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.state.CodeState;

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

    /**
     * Get the current code configuration metadata.
     *
     * <p>Returns a {@link CodeConfig} containing rotor IDs, current positions,
     * reflector ID, and plugboard configuration. The positions reflect the
     * current state (after any processing), not the original positions.</p>
     *
     * @return current code configuration, or null if not configured
     * @since 1.0
     */
    CodeConfig getConfig();

    /**
     * Get detailed current code state snapshot.
     *
     * <p>Returns a {@link CodeState} containing rotor IDs, current positions,
     * notch distances, reflector ID, and plugboard. This provides more detail
     * than {@link #getConfig()}, including distance to each rotor's notch.</p>
     *
     * @return current code state, or null if not configured
     * @since 1.0
     */
    CodeState getCodeState();

    /**
     * Reset rotor positions to their original values.
     *
     * <p><b>Behavior:</b></p>
     * <ul>
     *   <li>Returns all rotor positions to their <b>original values</b> at configuration time</li>
     *   <li>Maintains rotor selection, reflector selection, and plugboard</li>
     *   <li>Does NOT change the code configuration itself</li>
     * </ul>
     *
     * <p><b>Layer Responsibility:</b> Machine handles mechanical reset (positions).
     * Engine handles history and statistics (NOT reset by this method).</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * machine.setCode(code);  // positions = ['O', 'D', 'X']
     * machine.process('A');   // positions advance to ['O', 'D', 'Y']
     * machine.process('B');   // positions advance to ['O', 'D', 'Z']
     * machine.reset();        // positions return to ['O', 'D', 'X']
     * </pre>
     *
     * @throws IllegalStateException if machine is not configured
     * @since 1.0
     */
    void reset();

    /**
     * Set rotor positions to specific values without reconfiguring.
     *
     * <p>This method allows advancing rotors to a specific state, useful
     * when loading snapshots or restoring saved machine states.</p>
     *
     * @param positions rotor positions as string (left→right), e.g., "ODX"
     * @throws IllegalStateException if machine is not configured
     * @throws IllegalArgumentException if positions length doesn't match rotor count
     * @since 1.0
     */
    void setPositions(String positions);
}
