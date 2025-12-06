package enigma.machine.component.code;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

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


    Alphabet getAlphabet();
}

