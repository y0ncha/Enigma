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

    @Override
    public char toChar(int idx) {
        if (!idxInbound(idx)) {
            throw new IllegalArgumentException("Invalid index for this machine: " + idx);
        }
        return alphabet.charAt(idx);
    }

    @Override
    public int toIdx(char ch) {
        if (!charInbound(ch)) {
            throw new IllegalArgumentException("Invalid character for this machine: " + ch);
        }
        return alphabet.indexOf(ch);
    }

    @Override
    public boolean charInbound(char ch) {
        return alphabet.contains(ch);
    }

    @Override
    public boolean idxInbound(int idx) {
        return 0 <= idx && idx < alphabet.size();
    }


    @Override
    public int size() {return alphabet.size(); }
}