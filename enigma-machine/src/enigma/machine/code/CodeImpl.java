package enigma.machine.code;

import enigma.machine.alphabet.Alphabet;
import enigma.machine.reflector.Reflector;
import enigma.machine.rotor.Rotor;

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
    private final List<Rotor> rotors;        // left → right (index 0 = leftmost)
    private final Reflector reflector;

    // metadata (config)
    private final List<Integer> rotorIds;    // left → right
    private final List<Integer> positions;   // numeric positions (0..|ABC|-1)
    private final String reflectorId;        // "I", "II", ...

    /**
     * Create a new immutable code instance.
     *
     * @param rotors active rotors in left→right order (index 0 = leftmost)
     * @param reflector active reflector
     * @param rotorIds rotor numeric ids in left→right order
     * @param positions rotor start positions (0-based)
     * @param reflectorId reflector identifier
     * @since 1.0
     */
    public CodeImpl(Alphabet alphabet, List<Rotor> rotors,
                    Reflector reflector,
                    List<Integer> rotorIds,
                    List<Integer> positions,
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
    public List<Integer> getPositions() {
        return positions;
    }

    @Override
    public String getReflectorId() {
        return reflectorId;
    }

    @Override
    public Alphabet getAlphabet() { return alphabet; }
}