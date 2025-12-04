package enigma.machine.component.keyboard;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Enigma keyboard and lampboard.
 * 
 * <p>This class handles the conversion between characters and alphabet indices.
 * The keyboard converts input characters to indices, and the lampboard (lightKey)
 * converts output indices back to characters.</p>
 * 
 * <p><strong>Example with alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ":</strong></p>
 * <pre>
 *   process('A') returns 0
 *   process('Z') returns 25
 *   lightKey(0) returns 'A'
 *   lightKey(25) returns 'Z'
 * </pre>
 */
public class KeyboardImpl implements Keyboard {
    
    private final String alphabet;
    private final Map<Character, Integer> charToIndex;
    
    /**
     * Constructs a keyboard with the specified alphabet.
     * 
     * @param alphabet the alphabet string defining valid characters and their indices
     */
    public KeyboardImpl(String alphabet) {
        this.alphabet = alphabet;
        this.charToIndex = new HashMap<>();
        for (int i = 0; i < alphabet.length(); i++) {
            charToIndex.put(alphabet.charAt(i), i);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException if the character is not in the alphabet
     */
    @Override
    public int process(char input) {
        Integer index = charToIndex.get(input);
        if (index == null) {
            throw new IllegalArgumentException(
                "Character '" + input + "' is not in the alphabet: " + alphabet);
        }
        return index;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws IndexOutOfBoundsException if the index is out of alphabet range
     */
    @Override
    public char lightKey(int input) {
        if (input < 0 || input >= alphabet.length()) {
            throw new IndexOutOfBoundsException(
                "Index " + input + " is out of range for alphabet of size " + alphabet.length());
        }
        return alphabet.charAt(input);
    }
}
