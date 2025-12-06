package enigma.machine.component.keyboard;

import enigma.machine.component.alphabet.Alphabet;

/**
 * Default {@link Keyboard} implementation backed by an {@link Alphabet}.
 * Validates characters and converts between char and numeric indices.
 *
 * @since 1.0
 */
public class KeyboardImpl implements Keyboard {

    private final Alphabet alphabet;

    /**
     * Create a keyboard that uses the provided alphabet.
     *
     * @param alphabet alphabet instance used for conversions
     * @since 1.0
     */
    public KeyboardImpl(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(char input) {
        int index = alphabet.indexOf(input);
        if (index < 0) {
            throw new IllegalArgumentException("Invalid character for this machine: " + input);
        }
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char lightKey(int input) {
        return alphabet.charAt(input);
    }

    @Override
    public int size() {return alphabet.size(); }
}