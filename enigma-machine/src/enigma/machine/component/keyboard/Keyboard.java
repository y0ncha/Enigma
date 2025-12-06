package enigma.machine.component.keyboard;

/**
 * Keyboard adapter that converts between characters and internal indices.
 *
 * <p>The Keyboard is the boundary between the char (symbol) world and the
 * int (index) world [0, alphabetSize). Implementations must validate input
 * and provide bidirectional conversion.</p>
 *
 * @since 1.0
 */
public interface Keyboard {

    /**
     * Convert an internal index to its alphabet character.
     *
     * @param idx internal index (0..alphabetSize-1)
     * @return corresponding alphabet character
     */
    char toChar(int idx);

    /**
     * Convert an alphabet character to its internal index.
     *
     * @param ch character from the alphabet
     * @return corresponding internal index (0..alphabetSize-1)
     */
    int toIdx(char ch);

    /**
     * Check if a character is valid for this keyboard's alphabet.
     *
     * @param ch character to test
     * @return true if ch is in the alphabet
     */
    boolean charInbound(char ch);

    /**
     * Check if an index is valid for this keyboard's alphabet.
     *
     * @param idx index to test
     * @return true if idx is in [0, alphabetSize)
     */
    boolean idxInbound(int idx);

    /**
     * Get the alphabet size.
     *
     * @return number of characters in the alphabet
     */
    int size();
}
