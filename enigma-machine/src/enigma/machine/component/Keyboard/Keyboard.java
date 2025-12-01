package enigma.machine.component.keyboard;

/**
 * Represents the keyboard/lampboard component of the Enigma machine.
 * <p>
 * The keyboard converts input characters to their numeric positions
 * in the alphabet for internal processing. The lampboard (lightKey)
 * converts processed positions back to characters for output.
 * </p>
 */
public interface Keyboard {

    /**
     * Converts an input character to its position in the alphabet.
     * <p>
     * This simulates pressing a key on the Enigma keyboard,
     * translating the character to a 0-based position for rotor processing.
     * </p>
     *
     * @param input the character pressed on the keyboard
     * @return the 0-based position of the character in the alphabet
     */
    int process(int input);

    /**
     * Converts a position back to its corresponding character.
     * <p>
     * This simulates the lamp lighting up on the Enigma lampboard,
     * showing the encrypted/decrypted output character.
     * </p>
     *
     * @param input the 0-based position to convert to a character
     * @return the character at the specified position in the alphabet
     */
    char lightKey(int input);
}
