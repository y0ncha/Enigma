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
     * Converts an input position to its corresponding keyboard position.
     * <p>
     * This simulates pressing a key on the Enigma keyboard,
     * translating the position for rotor processing.
     * </p>
     *
     * @param input the input position (may represent a character index)
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
