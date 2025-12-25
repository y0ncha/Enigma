package enigma.shared.dto.tracer;

/**
 * Trace of a signal passing through a single rotor.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <p>Records the transformation of a signal through one rotor in either
 * forward (right→left) or backward (left→right) direction. Includes both
 * int indices (internal model) and char symbols (alphabet mapping).</p>
 *
 * <h2>Field Semantics</h2>
 * <ul>
 *   <li><b>id:</b> rotor identifier (e.g., 1, 2, 3) from specification</li>
 *   <li><b>rotorIndex:</b> position in rotor stack (0 = leftmost, 2 = rightmost for 3 rotors)</li>
 *   <li><b>entryIndex:</b> internal index entering the rotor (0..alphabetSize-1)</li>
 *   <li><b>exitIndex:</b> internal index exiting the rotor (0..alphabetSize-1)</li>
 *   <li><b>entryChar:</b> alphabet character corresponding to entryIndex</li>
 *   <li><b>exitChar:</b> alphabet character corresponding to exitIndex</li>
 * </ul>
 *
 * <p><b>Direction Context:</b> The meaning of entry/exit depends on signal direction:
 * forward pass entry is from the right, backward pass entry is from the left.</p>
 *
 * @param id rotor identifier from spec
 * @param rotorIndex rotor position (index 0 = leftmost)
 * @param entryIndex internal index entering the rotor (0..alphabetSize-1)
 * @param exitIndex internal index exiting the rotor (0..alphabetSize-1)
 * @param entryChar alphabet letter entering the rotor
 * @param exitChar alphabet letter exiting the rotor
 * @since 1.0
 */
public record RotorTrace(
        int id,
        int rotorIndex,   // index 0 = leftmost, 1 = next to right, ...
        int entryIndex,   // numeric index entering the rotor
        int exitIndex,    // numeric index exiting the rotor
        char entryChar,   // letter entering the rotor
        char exitChar     // letter exiting the rotor
)
{}
