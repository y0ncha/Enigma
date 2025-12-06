package enigma.machine.component.alphabet;

/**
 * Immutable representation of the machine alphabet.
 *
 * <p><b>Module:</b> enigma-machine</p>
 *
 * <p>The alphabet is stored as a sequence of characters and provides
 * helper methods to query size and map between characters and indices.</p>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Alphabet must be non-null and non-empty</li>
 *   <li>All characters must be unique</li>
 *   <li>Length must be even (loader validation)</li>
 * </ul>
 *
 * <h2>Index Mapping</h2>
 * <p>Characters are mapped to 0-based indices in the order they appear
 * in the alphabet string. The {@link enigma.machine.component.keyboard.Keyboard}
 * uses this mapping for all char ↔ index conversions.</p>
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
     * @return the letters string (immutable)
     * @since 1.0
     */
    public String getLetters() {
        return letters;
    }

    /**
     * Return the number of characters in the alphabet.
     *
     * @return alphabet length (always even per loader validation)
     * @since 1.0
     */
    public int size() {
        return letters.length();
    }

    /**
     * Return the zero-based index of the given character.
     *
     * @param c character to look up
     * @return index of the character (0..size-1), or -1 if not present
     * @since 1.0
     */
    public int indexOf(char c) {
        return letters.indexOf(c);
    }

    /**
     * Return the character at the given zero-based index.
     *
     * @param index zero-based index (0..size-1)
     * @return character at index
     * @throws IndexOutOfBoundsException if index is out of range
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