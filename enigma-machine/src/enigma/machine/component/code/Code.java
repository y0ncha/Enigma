package enigma.machine.component.code;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;
import enigma.shared.dto.config.CodeConfig;

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
     * The machine alphabet used by this code.
     *
     * <p>This alphabet defines the valid character set and the mapping
     * between characters and internal indices used by rotors/reflector.</p>
     *
     * @return {@link Alphabet} instance for this code
     */
    Alphabet getAlphabet();

    /**
     * Configuration metadata describing this code.
     *
     * <p>Returns a {@link CodeConfig} record containing the rotor id list
     * (left→right), the starting positions as characters, and the
     * reflector identifier. The returned object is intended for external
     * consumers and should be treated as immutable.</p>
     *
     * @return {@link CodeConfig} describing the active code
     */
    CodeConfig getConfig();

    List<Integer> getNotchDist();

    /**
     * Reset all rotor positions to their original configured values.
     * 
     * <p>This returns all rotors to the positions they had when the code
     * was initially created, allowing the machine to restart encryption
     * from the same starting point.</p>
     */
    void reset();

    /**
     * Set rotor positions to specific values.
     * 
     * <p>This method allows manually advancing or setting rotor positions
     * without going through a full reconfiguration. Used primarily when
     * loading snapshots to restore the exact rotor state.</p>
     *
     * @param positions string of position characters (left→right), length must match rotor count
     * @throws IllegalArgumentException if positions length doesn't match rotor count
     */
    void setPositions(String positions);
}
