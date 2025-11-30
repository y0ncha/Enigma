package enigma.machine.component.alphabet;

/**
 * Immutable representation of the machine alphabet.
 * <p>
 * The alphabet is stored as a sequence of characters and provides
 * helper methods to query size and map between characters and indices.
 *
 * @since 1.0
 */
public class Alphabet {
    private final String letters;

    public Alphabet(String letters) {
        this.letters = letters;
    }

    /**
     * Return number of characters in the alphabet.
     *
     * @return alphabet length
     * @since 1.0
     */
    public int size() {
        return letters.length();
    }

    /**
     * Return zero-based index of the given character.
     *
     * @param c character to look up
     * @return index of the character or -1 if not present
     * @since 1.0
     */
    public int indexOf(char c) {
        return letters.indexOf(c);
    }

    /**
     * Return character at the given zero-based index.
     *
     * @param index zero-based index
     * @return character at index
     * @since 1.0
     */
    public char charAt(int index) {
        return letters.charAt(index);
    }

    /**
     * Check whether the alphabet contains the given character.
     *
     * @param c character to test
     * @return true if the character is present, false otherwise
     * @since 1.0
     */
    public boolean contains(char c) {
        return letters.indexOf(c) >= 0;
    }
}