package enigma.machine.component.reflector;

/**
 * Reflector maps an internal index to its paired index via symmetric wiring.
 *
 * <p>The reflector sits at the leftmost position of the rotor stack and
 * provides a symmetric pairwise mapping: if process(i) = j, then process(j) = i.</p>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Mapping must be symmetric: process(process(i)) = i</li>
 *   <li>All indices in [0, alphabetSize) must be covered</li>
 * </ul>
 *
 * @since 1.0
 */
public interface Reflector {
    /**
     * Transform the input index through the reflector mapping.
     *
     * @param input index to reflect (0..alphabetSize-1)
     * @return reflected index (0..alphabetSize-1)
     * @since 1.0
     */
    int process(int input);

    /**
     * Get the reflector's identifier.
     *
     * @return reflector ID (e.g., "I", "II")
     */
    String getId();
}
