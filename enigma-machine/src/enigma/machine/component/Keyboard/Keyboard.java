package enigma.machine.component.keyboard;

/**
 * Represents the keyboard component of the Enigma machine.
 * The keyboard handles character input and output, converting between
 * characters and their numeric representations within the alphabet.
 */
public interface Keyboard {

    /**
     * Processes an input by mapping it through the keyboard.
     *
     * @param input the numeric index of the input character
     * @return the processed numeric index
     */
    int process(int input);

    /**
     * Converts a numeric index back to its corresponding character
     * and lights up the output key.
     *
     * @param input the numeric index to convert
     * @return the corresponding output character
     */
    char lightKey(int input);
}
