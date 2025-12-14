package enigma.machine.component.alphabet;

/**
 * Immutable alphabet mapping characters to indices.
 *
 * <p>Stores character sequence and provides bidirectional mapping
 * between characters and zero-based indices.</p>
 *
 * @since 1.0
 */
public class Alphabet {
    private final String letters;

    /**
     * Create alphabet from character sequence.
     *
     * @param letters character sequence for alphabet
     * @throws IllegalArgumentException if letters is null, empty, or contains duplicates
     * @since 1.0
     */
    public Alphabet(String letters) {
        if (letters == null) {
            throw new IllegalArgumentException("Alphabet letters cannot be null");
        }
        if (letters.isEmpty()) {
            throw new IllegalArgumentException("Alphabet letters cannot be empty");
        }
        java.util.Set<Character> seen = new java.util.HashSet<>();
        for (char c : letters.toCharArray()) {
            if (!seen.add(c)) {
                throw new IllegalArgumentException("Alphabet contains duplicate character: " + c);
            }
        }
        this.letters = letters;
    }

    /**
     * Return underlying letters string.
     *
     * @return alphabet letters
     * @since 1.0
     */
    public String getLetters() {
        return letters;
    }

    /**
     * Return alphabet size.
     *
     * @return number of characters in alphabet
     * @since 1.0
     */
    public int size() {
        return letters.length();
    }

    /**
     * Return index of character.
     *
     * @param c character to find
     * @return index of character, or -1 if not present
     * @since 1.0
     */
    public int indexOf(char c) {
        return letters.indexOf(c);
    }

    /**
     * Return character at index.
     *
     * @param index position in alphabet
     * @return character at index
     * @throws IndexOutOfBoundsException if index is out of range
     * @since 1.0
     */
    public char charAt(int index) {
        return letters.charAt(index);
    }

    /**
     * Check if alphabet contains character.
     *
     * @param c character to test
     * @return true if present, false otherwise
     * @since 1.0
     */
    public boolean contains(char c) {
        return letters.indexOf(c) >= 0;
    }
}