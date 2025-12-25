package enigma.engine.factory;

import enigma.engine.exception.InvalidConfigurationException;
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
     * @throws InvalidConfigurationException if alphabet is null
     */
    public RotorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) {
            throw new InvalidConfigurationException(
                "Rotor factory initialization failed: Alphabet is missing");
        }
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
     * @throws InvalidConfigurationException if column lengths don't match alphabet size
     */
    @Override
    public Rotor create(RotorSpec spec) {
        // Pass char[] columns directly (preserve XML character rows). RotorImpl handles chars.
        char[] rightChars = spec.getRightColumn();
        char[] leftChars = spec.getLeftColumn();
        if (rightChars.length != leftChars.length || rightChars.length != alphabet.size()) {
            throw new InvalidConfigurationException(
                String.format(
                    "Rotor creation failed for rotor %d: Column length mismatch. " +
                    "Right column length: %d, Left column length: %d, Alphabet size: %d. " +
                    "Fix: Ensure the XML rotor specification has columns matching the alphabet size.",
                    spec.id(), rightChars.length, leftChars.length, alphabet.size()));
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
