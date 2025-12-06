package enigma.machine.component.code;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Immutable implementation of {@link Code} that stores active components
 * and configuration metadata.
 *
 * @since 1.0
 */
public class CodeImpl implements Code {

    // active components
    private final Alphabet alphabet;
    private final List<Rotor> rotors;        // right → left
    private final Reflector reflector;

    // metadata (config)
    private final List<Integer> rotorIds;    // right → left
    private final List<Character> positions;   // rotor window positions as characters (e.g., 'A', 'B', 'C')
    private final String reflectorId;        // "I", "II", ...

    /**
     * Create a new immutable code instance.
     *
     * @param rotors active rotors in right→left order
     * @param reflector active reflector
     * @param rotorIds rotor numeric ids in right→left order
     * @param positions rotor start positions as characters from the alphabet
     * @param reflectorId reflector identifier
     * @since 1.0
     */
    public CodeImpl(Alphabet alphabet, List<Rotor> rotors,
                    Reflector reflector,
                    List<Integer> rotorIds,
                    List<Character> positions,
                    String reflectorId) {
        this.alphabet = alphabet;

        this.rotors = List.copyOf(rotors);
        this.reflector = reflector;
        this.rotorIds = List.copyOf(rotorIds);
        this.positions = List.copyOf(positions);
        this.reflectorId = reflectorId;
    }

    @Override
    public List<Rotor> getRotors() {
        return rotors;
    }

    @Override
    public Reflector getReflector() {
        return reflector;
    }

    @Override
    public List<Integer> getRotorIds() {
        return rotorIds;
    }

    @Override
    public Alphabet getAlphabet() { return alphabet; }
}