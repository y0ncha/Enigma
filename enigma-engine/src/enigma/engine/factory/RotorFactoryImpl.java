package enigma.engine.factory;

import enigma.machine.component.rotor.RotorImpl;
import enigma.shared.spec.RotorSpec;
import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.rotor.Rotor;

/**
 * Default {@link RotorFactory} implementation producing {@link RotorImpl} instances.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>This factory is bound to an {@link Alphabet} instance and constructs rotors
 * using the mechanical column-rotation model. The factory:</p>
 * <ul>
 *   <li>Extracts right/left character columns from {@link RotorSpec}</li>
 *   <li>Validates column lengths match alphabet size</li>
 *   <li>Constructs {@link RotorImpl} with notch index and rotor ID</li>
 * </ul>
 *
 * <h2>Wiring Order</h2>
 * <p>Character columns are passed directly to {@link RotorImpl} without modification.
 * The loader ensures columns are in correct XML row order (top→bottom).</p>
 *
 * <h2>Mechanical Model</h2>
 * <p>The constructed rotor uses physical rotation (moving top row to bottom)
 * and the two-column lookup model that accurately reflects real Enigma behavior.</p>
 *
 * @since 1.0
 * @see RotorImpl
 */
public class RotorFactoryImpl implements RotorFactory {

    private final Alphabet alphabet;

    /**
     * Create a rotor factory bound to the given alphabet.
     *
     * @param alphabet alphabet for size validation and rotor construction
     * @throws IllegalArgumentException if alphabet is null
     */
    public RotorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    /**
     * Create a rotor from the given specification.
     *
     * <p>Extracts right/left character columns from the spec and constructs
     * a {@link RotorImpl} using the mechanical column-rotation model. Columns
     * are passed as-is without modification (XML row order).</p>
     *
     * @param spec rotor specification with ID, columns, and notch index
     * @return {@link RotorImpl} instance ready for position setting
     * @throws IllegalStateException if column lengths don't match alphabet size
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
