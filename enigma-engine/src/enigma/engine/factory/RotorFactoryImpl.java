package enigma.engine.factory;

import enigma.engine.model.RotorSpec;
import enigma.machine.alphabet.Alphabet;
import enigma.machine.rotor.Rotor;
import enigma.machine.rotor.RotorImpl;

/**
 * Default {@link RotorFactory} implementation producing {@link RotorImpl} instances.
 *
 * <p>This implementation is bound to an {@link Alphabet} instance.
 * The factory constructs rotors from a {@link RotorSpec} and an initial position
 * (0-based index). Caller is expected to validate the spec and position range.</p>
 *
 * @since 1.0
 */
public class RotorFactoryImpl implements RotorFactory {

    private final Alphabet alphabet;

    /**
     * Create a rotor factory bound to the given alphabet.
     *
     * @param alphabet alphabet used for rotor construction
     * @throws IllegalArgumentException if alphabet is null
     */
    public RotorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rotor create(RotorSpec spec, int startPosition) {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");

        int[] forward = spec.getForwardMapping();
        int[] backward = spec.getBackwardMapping();
        int notch = spec.notchIndex();

        // RotorImpl constructor expects alphabet, forward/backward maps, notch and start position
        return new RotorImpl(alphabet, forward, backward, notch, startPosition);
    }
}
