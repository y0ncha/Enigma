package enigma.machine.component.keyboard;

import enigma.machine.component.alphabet.Alphabet;

/**
 * Default {@link Keyboard} implementation backed by an {@link Alphabet}.
 *
 * <p><b>Module:</b> enigma-machine</p>
 *
 * <h2>Responsibility</h2>
 * <p>The Keyboard is the <b>sole boundary</b> between the char (symbol) world
 * and the int (index) world in the Enigma machine. All char ↔ int conversions
 * must pass through the keyboard:</p>
 * <ul>
 *   <li>{@link #toIdx(char)} converts user input chars to internal indices [0, alphabetSize)</li>
 *   <li>{@link #toChar(int)} converts internal indices back to output chars</li>
 * </ul>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>All indices must be in range [0, alphabetSize)</li>
 *   <li>All chars must exist in the configured alphabet</li>
 *   <li>Conversions are deterministic and bijective within valid ranges</li>
 * </ul>
 *
 * @since 1.0
 */
public class KeyboardImpl implements Keyboard {

    private final Alphabet alphabet;

    /**
     * Create a keyboard that uses the provided alphabet.
     *
     * @param alphabet alphabet instance used for char ↔ index conversions
     * @since 1.0
     */
    public KeyboardImpl(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char toChar(int idx) {
        if (!idxInbound(idx)) {
            throw new IllegalArgumentException("Index out of range: " + idx);
        }
        return alphabet.charAt(idx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int toIdx(char ch) {
        if (!charInbound(ch)) {
            throw new IllegalArgumentException("Character not in alphabet: " + ch);
        }
        return alphabet.indexOf(ch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean charInbound(char ch) {
        return alphabet.contains(ch);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean idxInbound(int idx) {
        return 0 <= idx && idx < alphabet.size();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {return alphabet.size(); }
}