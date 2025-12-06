package enigma.machine.component.keyboard;

/**
 * Keyboard adapter that converts between characters and internal indices.
 * Implementations should validate input characters against the machine alphabet.
 *
 * @since 1.0
 */
public interface Keyboard {
    /**
     * Convert input character to internal index.
     *
     * @param input character to convert
     * @return internal index (0..alphabetSize-1)
     * @throws IllegalArgumentException when the character is not part of the machine alphabet
     */
    int process(char input);

    /**
     * Convert an internal index to the corresponding output character.
     *
     * @param input internal index
     * @return output character
     */
    char lightKey(int input);

    int size();
}
