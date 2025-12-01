package enigma.machine.component.rotor;

/**
 * Represents a rotor component of the Enigma machine.
 * <p>
 * A rotor performs character substitution through its internal wiring
 * and can rotate to change the substitution mapping. Rotors have a notch
 * position that triggers advancement of the next rotor.
 * </p>
 */
public interface Rotor {

    /**
     * Processes an input signal through the rotor's wiring.
     * <p>
     * The transformation depends on the current rotor position and
     * the direction of the signal (forward or backward pass).
     * </p>
     *
     * @param input     the input signal position (0-based index in the alphabet)
     * @param direction the direction of signal flow through the rotor
     * @return the transformed signal position
     */
    int process(int input, Direction direction);

    /**
     * Advances the rotor by one position.
     * <p>
     * When a rotor passes its notch position, it triggers the next rotor
     * to advance as well (turnover mechanism).
     * </p>
     *
     * @return {@code true} if this rotor passed its notch and the next
     *         rotor should advance; {@code false} otherwise
     */
    boolean advance();
}
