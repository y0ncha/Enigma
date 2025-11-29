package enigma.machine.component.rotor;

public interface Rotor {

    /**
     * Advance the rotor by one step.
     * @return true if the next rotor to the left should also advance (notch engaged), false otherwise.
     */
    boolean advance();

    /**
     * Process a signal through the rotor in the given direction.
     * @param index    input index (0..alphabetSize-1)
     * @param direction FORWARD (right→left) or BACKWARD (left→right)
     * @return output index after passing through the rotor
     */
    int process(int index, Direction direction);

    /**
     * @return the current rotor position (0..alphabetSize-1)
     */
    int getPosition();

    /**
     * Set the current rotor position (used when building code).
     */
    void setPosition(int position);

    /**
     * @return the notch position (0..alphabetSize-1) that triggers stepping of the next rotor.
     */
    int getNotch();
}