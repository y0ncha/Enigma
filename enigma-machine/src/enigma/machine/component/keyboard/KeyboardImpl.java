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
     * Convert an internal index to its corresponding alphabet character.
     *
     * @param idx internal index (0..alphabetSize-1)
     * @return alphabet character at that index
     * @throws IllegalArgumentException if index is out of bounds
     */
    @Override
    public char toChar(int idx) {
        if (!idxInbound(idx)) {
            throw new IllegalArgumentException("Invalid index for this machine: " + idx);
        }
        return alphabet.charAt(idx);
    }

    /**
     * Convert an alphabet character to its corresponding internal index.
     *
     * @param ch character from the alphabet
     * @return internal index (0..alphabetSize-1)
     * @throws IllegalArgumentException if character is not in the alphabet
     */
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