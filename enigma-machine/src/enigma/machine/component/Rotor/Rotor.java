package enigma.machine.component.rotor;

/**
 * Represents a rotor component of the Enigma machine.
 * Each rotor performs a substitution cipher and can rotate,
 * changing the mapping for each subsequent character.
 */
public interface Rotor {

    /**
     * Processes the input signal through the rotor's wiring.
     * The mapping differs based on the direction of signal flow.
     *
     * @param input the numeric index of the input letter
     * @param direction the direction of signal flow (FORWARD or BACKWARD)
     * @return the numeric index of the output letter
     */
    int process(int input, Direction direction);

    /**
     * Advances the rotor by one position.
     *
     * @return true if the notch is at the window position (triggers next rotor),
     *         false otherwise
     */
    boolean advance();
}
