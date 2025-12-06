package enigma.shared.dto.tracer;

/**
 * Trace of a signal passing through the reflector.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <p>Records the symmetric transformation at the reflector position (leftmost
 * in the rotor stack). The reflector maps an entry index to its paired exit
 * index via symmetric wiring.</p>
 *
 * <h2>Field Semantics</h2>
 * <ul>
 *   <li><b>entryIndex:</b> internal index before reflector mapping (0..alphabetSize-1)</li>
 *   <li><b>exitIndex:</b> internal index after reflector mapping (0..alphabetSize-1)</li>
 * </ul>
 *
 * <p><b>Invariant:</b> Due to reflector symmetry, if entryIndex maps to exitIndex,
 * then exitIndex maps back to entryIndex.</p>
 *
 * @param entryIndex index before reflector mapping (0..alphabetSize-1)
 * @param exitIndex index after reflector mapping (0..alphabetSize-1)
 * @since 1.0
 */
public record ReflectorTrace(
        int entryIndex,   // index before reflector mapping
        int exitIndex    // index after reflector mapping
)
{}
