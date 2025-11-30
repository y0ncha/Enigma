package enigma.engine.components.model;

/**
 * Represents an ordered set of letters used in the Enigma machine.
 * Provides utility methods for accessing and manipulating the alphabet,
 * such as retrieving its size, finding the index of a character, and
 * getting a character at a specific index.
 */
public class Alphabet {
    private final String letters; //  "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

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

    public String getLetters() {
        return letters;
    }

    public int size() {
        return letters.length();
    }

    public int indexOf(char ch) {
        return letters.indexOf(ch);
    }

    public char charAt(int index) {
        return letters.charAt(index);
    }
}
