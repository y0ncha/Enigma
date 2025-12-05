package enigma.machine.component.keyboard;

/**
 * Represents the keyboard and lampboard of the Enigma machine.
 * 
 * <p>The keyboard converts character input to alphabet indices for
 * processing through the rotors, and the lampboard converts output
 * indices back to characters for display.</p>
 * 
 * <p>In a real Enigma machine, the keyboard had mechanical switches
 * that sent electrical signals through the rotor assembly, and the
 * lampboard would illuminate to show the encrypted character.</p>
 */
public interface Keyboard {
    
    /**
     * Processes a character input from the keyboard.
     * 
     * <p>Converts the input character to its corresponding alphabet index
     * for processing through the rotor assembly.</p>
     * 
     * @param input the character pressed on the keyboard
     * @return the alphabet index (0-based) for this character
     * @throws IllegalArgumentException if the character is not in the alphabet
     */
    int process(char input);
    
    /**
     * Lights the lampboard key for the given index.
     * 
     * <p>Converts an alphabet index back to its corresponding character
     * for display on the lampboard.</p>
     * 
     * @param input the alphabet index (0-based)
     * @return the character at this index in the alphabet
     */
    char lightKey(int input);
}
