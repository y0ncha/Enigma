package enigma.engine.factory;

import enigma.machine.component.rotor.RotorImpl;
import enigma.shared.spec.RotorSpec;
import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.rotor.Rotor;

/**
 * Default {@link RotorFactory} implementation producing {@link RotorImpl} instances.
 *
 * <p>This factory is bound to an {@link Alphabet} instance and constructs rotors
 * using the mechanical column-rotation model. The factory:</p>
 * <ul>
 *   <li>Validates the specification and start position</li>
 *   <li>Extracts the forward mapping from the specification</li>
 *   <li>Builds the notch index from the specification</li>
 *   <li>Constructs the mechanical rotor</li>
 * </ul>
 *
 * <h2>Mapping Conventions</h2>
 * <p>Mappings are inherited directly from {@link RotorSpec}:</p>
 * <ul>
 *   <li>{@code forwardMapping}: index = right-side position, value = left-side position
 *       (right→left, used on the forward path toward the reflector)</li>
 *   <li>{@code backwardMapping}: index = left-side position, value = right-side position
 *       (left→right, used on the return path from the reflector)</li>
 * </ul>
 *
 * <p>This factory does <b>not</b> modify or invert the mappings; it passes them
 * directly to the {@link RotorImpl} constructor.</p>
 *
 * @since 1.0
 * @see RotorImpl
 */
public class RotorFactoryImpl implements RotorFactory {

    private final Alphabet alphabet;

    /**
     * Create a rotor factory bound to the given alphabet.
     *
     * @param alphabet alphabet for bounds checking and rotor construction
     * @throws IllegalArgumentException if alphabet is null
     */
    public RotorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This is the primary factory method that creates rotors using the
     * mechanical column-rotation model ({@link RotorImpl}).</p>
     */
    @Override
    public Rotor create(RotorSpec spec) {
        // Pass char[] columns directly (preserve XML character rows). RotorImpl handles chars.
        char[] rightChars = spec.getRightColumn();
        char[] leftChars = spec.getLeftColumn();
        if (rightChars.length != leftChars.length || rightChars.length != alphabet.size()) {
            throw new IllegalStateException("RotorSpec column length mismatch or not equal to alphabet size");
        }

        return new RotorImpl(
                rightChars,
                leftChars,
                spec.notchIndex(),
                this.alphabet.size(),
                spec.id()
        );
    }
}
