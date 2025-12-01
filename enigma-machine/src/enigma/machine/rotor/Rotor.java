package enigma.machine.rotor;

/**
 * Rotor abstraction providing processing and stepping behavior.
 *
 * @since 1.0
 */
public interface Rotor {

    /**
     * Advance the rotor by one step.
     *
     * @return true if the next rotor to the left should also advance (notch engaged), false otherwise
     */
    boolean advance();

    /**
     * Process a signal through the rotor in the given direction.
     *
     * @param index input index (0..alphabetSize-1)
     * @param direction FORWARD (right→left) or BACKWARD (left→right)
     * @return output index after passing through the rotor
     */
    int process(int index, Direction direction);

    /**
     * Get the current rotor position.
     *
     * @return current position (0..alphabetSize-1)
     */
    int getPosition();

    /**
     * Set the current rotor position.
     *
     * @param position new position (0..alphabetSize-1)
     */
    void setPosition(int position);

    /**
     * Get the notch position which triggers stepping of the next rotor.
     *
     * @return notch position (0..alphabetSize-1)
     */
    int getNotch();
}