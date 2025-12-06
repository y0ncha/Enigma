package enigma.machine.component.code;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Represents a machine code configuration: rotors, reflector and metadata.
 *
 * <p><b>Module:</b> enigma-machine</p>
 *
 * <p>A Code bundles the active components (rotors, reflector) with their
 * configuration metadata (IDs, positions, alphabet). Rotors are ordered
 * left→right with index 0 representing the leftmost rotor.</p>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Rotors list order matches configuration order (left→right)</li>
 *   <li>All components share the same alphabet</li>
 *   <li>Rotor IDs and positions lists must match rotors list size</li>
 * </ul>
 *
 * @since 1.0
 */
public interface Code {

    // Components
    /**
     * Active rotors in left→right order (index 0 = leftmost).
     *
     * @return immutable ordered list of {@link Rotor}
     */
    List<Rotor> getRotors();

    /**
     * Active reflector for the current code.
     *
     * @return reflector instance
     */
    Reflector getReflector();

    /**
     * Rotor IDs corresponding to the rotors list (left→right, index 0 = leftmost).
     *
     * @return immutable list of rotor IDs
     */
    List<Integer> getRotorIds();

    /**
     * Machine alphabet used by all components in this code.
     *
     * @return alphabet instance
     */
    Alphabet getAlphabet();
}

