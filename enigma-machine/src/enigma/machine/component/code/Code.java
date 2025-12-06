package enigma.machine.component.code;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;
import enigma.shared.dto.config.CodeConfig;

import java.util.List;

/**
 * Represents a machine code configuration: rotors, reflector and metadata.
 * Implementations are expected to provide ordered rotor instances (left→right)
 * together with their configuration metadata.
 *
 * @since 1.0
 */
public interface Code {

    // Components
    /**
     * Active rotors in left-to-right order (index 0 = leftmost).
     *
     * @return ordered list of {@link Rotor}
     */
    List<Rotor> getRotors();

    /**
     * Active reflector for the current code.
     *
     * @return reflector instance
     */
    Reflector getReflector();


    /**
     * Rotor ids corresponding to the rotors list (left→right, index 0 = leftmost).
     *
     * @return list of rotor ids
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
}
