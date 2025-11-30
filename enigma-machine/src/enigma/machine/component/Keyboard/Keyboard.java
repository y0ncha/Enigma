package enigma.machine.component.keyboard;

/**
 * Represents the keyboard component of the Enigma machine.
 * The keyboard handles character input and output, converting between
 * characters and their numeric representations within the alphabet.
 */
public interface Keyboard {

    /**
     * Processes an input character by converting it to its numeric index.
     *
     * @param input the numeric representation of the input character
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
