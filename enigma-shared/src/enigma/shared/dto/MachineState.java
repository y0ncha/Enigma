package enigma.shared.dto;

import enigma.shared.dto.config.CodeConfig;

/**
 * Immutable snapshot of the engine-visible machine state.
 * Example:
 * <pre>MachineState s = engine.getMachineState(); // read-only snapshot</pre>
 *
 * Important (concise):
 * - {@code originalConfig} and {@code currentConfig} are DTOs (left→right rotor IDs, positions as chars, reflector id).
 * - {@code stringsProcessed} is a best-effort counter snapshot and may change concurrently.
 *
 * @param numOfRotors number of rotors configured
 * @param numOfReflectors number of reflectors in the loaded spec
 * @param stringsProcessed processed-strings counter (snapshot)
 * @param originalConfig canonical CodeConfig used for reset
 * @param currentConfig currently active CodeConfig
 */
public record MachineState(
        int numOfRotors,
        int numOfReflectors,
        int stringsProcessed,
        CodeConfig originalConfig,
        CodeConfig currentConfig
) {
}
