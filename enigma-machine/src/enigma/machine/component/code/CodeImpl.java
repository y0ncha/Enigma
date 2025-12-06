package enigma.machine.component.code;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;
import enigma.shared.dto.config.CodeConfig;

import java.util.List;

/**
 * Immutable implementation of {@link Code} that stores active components
 * and configuration metadata.
 *
 * <p><b>Module:</b> enigma-machine</p>
 *
 * <p>This class is a simple immutable container for runtime code configuration.
 * Construction is handled by the engine's {@link enigma.engine.factory.CodeFactory}.
 * All lists are defensively copied to ensure immutability.</p>
 *
 * @since 1.0
 */
public class CodeImpl implements Code {

    // active components
    private final Alphabet alphabet;
    private final List<Rotor> rotors;        // left → right (index 0 = leftmost)
    private final Reflector reflector;

    // metadata (config)
    private final List<Integer> rotorIds;    // left → right (index 0 = leftmost)
    private final List<Character> positions;   // rotor window positions as characters (e.g., 'A', 'B', 'C')
    private final String reflectorId;        // "I", "II", ...

    /**
     * Create a new immutable code instance.
     *
     * <p>All list parameters are defensively copied. Lists must not be null
     * and sizes must be consistent.</p>
     *
     * @param alphabet shared alphabet for all components
     * @param alphabet machine alphabet used by rotors and reflector
     * @param rotors active rotors in left→right order
     * @param reflector active reflector
     * @param rotorIds rotor numeric IDs in left→right order
     * @param positions rotor start positions as characters from the alphabet
     * @param reflectorId reflector identifier
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

    /** {@inheritDoc} */
    @Override
    public List<Rotor> getRotors() {
        return rotors;
    }

    /** {@inheritDoc} */
    @Override
    public Reflector getReflector() {
        return reflector;
    }

    /** {@inheritDoc} */
    @Override
    public List<Integer> getRotorIds() {
        return rotorIds;
    }

    /** {@inheritDoc} */
    @Override
    public Alphabet getAlphabet() { return alphabet; }

    /** {@inheritDoc} */
    @Override
    public CodeConfig getConfig() {
        return new CodeConfig(rotorIds, positions, reflectorId);
    }
}