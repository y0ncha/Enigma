package enigma.engine.factory;

import enigma.machine.rotor.MechanicalRotor;
import enigma.shared.spec.RotorSpec;
import enigma.machine.alphabet.Alphabet;
import enigma.machine.rotor.Rotor;
import enigma.machine.rotor.VirtualRotor;

import java.util.Objects;

/**
 * Default {@link RotorFactory} implementation producing {@link VirtualRotor} instances.
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
     * Default {@link RotorFactory} implementation.
     *
     * <p><b>Mapping conventions (inherited from {@link RotorSpec}):</b></p>
     * <ul>
     *   <li>{@code forwardMapping}: index = right-side position, value = left-side position
     *       (right→left, used on the forward path toward the reflector).</li>
     *   <li>{@code backwardMapping}: index = left-side position, value = right-side position
     *       (left→right, used on the return path from the reflector).</li>
     * </ul>
     *
     * <p>This factory does <b>not</b> modify or invert the mappings; it simply adapts
     * the immutable {@link RotorSpec} data into a runtime {@link VirtualRotor}.</p>
     */
    public RotorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rotor createVirtual(RotorSpec spec, int startPosition) {
        Objects.requireNonNull(spec, "spec must not be null");

        if (startPosition < 0 || startPosition >= alphabet.size()) {
            throw new IllegalArgumentException(
                    "startPosition out of range: " + startPosition +
                            " (alphabet size=" + alphabet.size() + ")"
            );
        }

        // Preserve directional semantics from RotorSpec:
        // forward:  right -> left
        // backward: left  -> right
        int[] forward = spec.getForwardMapping();   // defensive copy from spec
        int[] backward = spec.getBackwardMapping(); // defensive copy from spec

        int notchIndex = spec.notchIndex(); // 0-based notch index

        return new VirtualRotor(
                alphabet,
                forward,        // right→left
                backward,       // left→right
                notchIndex,
                startPosition
        );
    }

    @Override
    public Rotor createMechanical(RotorSpec spec, int startPosition) {
        Objects.requireNonNull(spec, "spec must not be null");

        if (startPosition < 0 || startPosition >= alphabet.size()) {
            throw new IllegalArgumentException(
                    "startPosition out of range: " + startPosition +
                            " (alphabet size=" + alphabet.size() + ")"
            );
        }

        return new MechanicalRotor(spec.getForwardMapping(), spec.notchIndex(), alphabet.size());
    }
}

