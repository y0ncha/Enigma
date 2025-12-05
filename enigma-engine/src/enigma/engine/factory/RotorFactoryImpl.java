package enigma.engine.factory;

import enigma.machine.rotor.RotorImpl;
import enigma.shared.spec.RotorSpec;
import enigma.machine.alphabet.Alphabet;
import enigma.machine.rotor.Rotor;
import enigma.machine.rotor.virtual.VirtualRotor;

import java.util.Objects;

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
 *   <li>Sets the initial position via {@link RotorImpl#setPosition(int)}</li>
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
    public Rotor create(RotorSpec spec, int startPosition) {
        validateInputs(spec, startPosition);

        RotorImpl rotor = buildMechanicalRotor(spec);
        rotor.setPosition(startPosition);
        return rotor;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #create(RotorSpec, int)} instead
     */
    @Override
    @Deprecated(since = "1.0", forRemoval = true)
    @SuppressWarnings("deprecation")
    public Rotor createVirtual(RotorSpec spec, int startPosition) {
        validateInputs(spec, startPosition);

        // Preserve directional semantics from RotorSpec:
        // forward:  right -> left
        // backward: left  -> right
        int[] forward = spec.getForwardMapping();
        int[] backward = spec.getBackwardMapping();
        int notchIndex = spec.notchIndex();

        return new VirtualRotor(
                alphabet,
                forward,
                backward,
                notchIndex,
                startPosition
        );
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #create(RotorSpec, int)} instead
     */
    @Override
    @Deprecated(since = "1.0", forRemoval = true)
    public Rotor createMechanical(RotorSpec spec, int startPosition) {
        return create(spec, startPosition);
    }

    // ---------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------

    /**
     * Validate the specification and start position.
     *
     * @param spec rotor specification to validate
     * @param startPosition initial position to validate
     * @throws NullPointerException if spec is null
     * @throws IllegalArgumentException if startPosition is out of range
     */
    private void validateInputs(RotorSpec spec, int startPosition) {
        Objects.requireNonNull(spec, "spec must not be null");

        if (startPosition < 0 || startPosition >= alphabet.size()) {
            throw new IllegalArgumentException(
                    "startPosition out of range: " + startPosition +
                            " (alphabet size=" + alphabet.size() + ")"
            );
        }
    }

    /**
     * Build a mechanical rotor from the specification.
     *
     * <p>Extracts the forward mapping, backward mapping, and notch index
     * from the spec and constructs a new {@link RotorImpl}.</p>
     *
     * @param spec rotor specification
     * @return new RotorImpl instance (position not yet set)
     */
    private RotorImpl buildMechanicalRotor(RotorSpec spec) {
        int[] forwardMapping = buildForwardMapping(spec);
        int[] backwardMapping = buildBackwardMapping(spec);
        int notchIndex = buildNotchIndex(spec);

        return new RotorImpl(
                forwardMapping,
                backwardMapping,
                notchIndex,
                alphabet.size(),
                spec.id()
        );
    }

    /**
     * Extract the forward mapping (right→left) from the specification.
     *
     * @param spec rotor specification
     * @return defensive copy of the forward mapping array
     */
    private int[] buildForwardMapping(RotorSpec spec) {
        return spec.getForwardMapping();
    }

    /**
     * Extract the backward mapping (left→right) from the specification.
     *
     * @param spec rotor specification
     * @return defensive copy of the backward mapping array
     */
    private int[] buildBackwardMapping(RotorSpec spec) {
        return spec.getBackwardMapping();
    }

    /**
     * Extract the notch index from the specification.
     *
     * @param spec rotor specification
     * @return notch index (0-based)
     */
    private int buildNotchIndex(RotorSpec spec) {
        return spec.notchIndex();
    }
}

