package enigma.engine.components.factory;

import enigma.engine.components.model.RotorSpec;
import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.rotor.Rotor;
import enigma.machine.component.rotor.RotorImpl;

/**
 * Default {@link RotorFactory} implementation producing {@link RotorImpl} instances.
 * This implementation is bound to an {@link Alphabet} instance.
 *
 * The factory constructs rotors from a {@link RotorSpec} and an initial position
 * (0-based index). Caller is expected to validate the spec and position range.
 */
public class RotorFactoryImpl implements RotorFactory {

    private final Alphabet alphabet;

    public RotorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    @Override
    public Rotor create(RotorSpec spec, int startPosition) {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");

        int[] forward = spec.getForwardMapping();
        int[] backward = spec.getBackwardMapping();
        int notch = spec.getNotchIndex();

        // RotorImpl constructor expects alphabet, forward/backward maps, notch and start position
        return new RotorImpl(alphabet, forward, backward, notch, startPosition);
    }
}
