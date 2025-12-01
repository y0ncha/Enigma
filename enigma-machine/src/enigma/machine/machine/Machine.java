package enigma.machine.machine;

import enigma.machine.code.Code;

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
}
