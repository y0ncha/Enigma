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

    /**
     * Create an Alphabet instance. The provided string must be non-null,
     * non-empty and contain unique characters.
     *
     * @param letters sequence of characters composing the alphabet
     * @throws IllegalArgumentException when letters is null, empty or contains duplicates
     * @since 1.0
     */
    public Alphabet(String letters) {
        if (letters == null) {
            throw new IllegalArgumentException("Alphabet letters cannot be null.");
        }
        if (letters.isEmpty()) {
            throw new IllegalArgumentException("Alphabet letters cannot be empty.");
        }
        java.util.Set<Character> seen = new java.util.HashSet<>();
        for (char c : letters.toCharArray()) {
            if (!seen.add(c)) {
                throw new IllegalArgumentException("Alphabet letters must be unique. Duplicate character: " + c);
            }
        }
        this.letters = letters;
    }

    /**
     * Return the underlying letters string.
     *
     * @return the letters string
     * @since 1.0
     */
    public String getLetters() {
        return letters;
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