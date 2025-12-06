package enigma.machine.component.rotor;

/**
 * Rotor abstraction providing signal processing and stepping behavior.
 *
 * <p>A rotor transforms electrical signals passing through it according to its
 * internal wiring, and can rotate (step) to change the transformation for
 * subsequent signals. All indices used by this interface are global alphabet
 * indices in the range [0, alphabetSize-1].</p>
 *
 * <h2>Signal Processing</h2>
 * <p>The {@link #process(int, Direction)} method transforms an input index to
 * an output index based on the rotor's wiring and current rotational position:</p>
 * <ul>
 *   <li>{@link Direction#FORWARD}: Signal travels right→left (toward reflector)</li>
 *   <li>{@link Direction#BACKWARD}: Signal travels left→right (from reflector)</li>
 * </ul>
 *
 * <h2>Stepping</h2>
 * <p>The {@link #advance()} method simulates the physical rotation of the rotor
 * wheel. In a real Enigma machine, this rotation changes the electrical path
 * through the rotor. The method returns {@code true} when the rotor's notch
 * position is reached, indicating that the next rotor (to the left) should
 * also advance.</p>
 *
 * <h2>Implementation</h2>
 * <p>The canonical implementation is {@link RotorImpl}, which uses the mechanical
 * column-rotation model that accurately reflects physical Enigma behavior.</p>
 *
 * @since 1.0
 * @see RotorImpl
 * @see Direction
 */
public interface Rotor {

    /**
     * Advance the rotor by one step (rotate the wheel).
     *
     * <p>This method simulates the physical rotation of the rotor. After
     * advancing, the rotor's internal wiring alignment changes, affecting
     * subsequent signal transformations.</p>
     *
     * @return {@code true} if the next rotor to the left should also advance
     *         (notch engaged), {@code false} otherwise
     */
    boolean advance();

    /**
     * Process a signal through the rotor in the given direction.
     *
     * <p>The input and output are alphabet indices (0..alphabetSize-1).
     * The transformation depends on the rotor's wiring and current position.</p>
     *
     * @param index input index (0..alphabetSize-1)
     * @param direction {@link Direction#FORWARD} (right→left) or
     *                  {@link Direction#BACKWARD} (left→right)
     * @return output index after passing through the rotor (0..alphabetSize-1)
     */
    int process(int index, Direction direction);

    /**
     * Get the current rotor position.
     *
     * <p>The position represents the letter visible in the machine window and
     * determines the rotor's electrical alignment with the rest of the circuit.</p>
     *
     * @return current character in the window
     */
    char getPosition();

    /**
     * Set the rotor to a specific position.
     *
     * <p>This method rotates the rotor until the window shows the desired
     * character. Used during machine configuration to set initial positions.</p>
     *
     * @param position target position character from the alphabet
     */
    void setPosition(char position);

    /**
     * Get the rotor's identifier.
     *
     * <p>Returns a stable, implementation-defined integer identifier for this
     * rotor instance or rotor type. Implementations SHOULD use this value for
     * tracing, logging and diagnostics (for example: a rotor model or catalog
     * number from the machine specification). This identifier is distinct from
     * the rotor's current rotational position and does not change when the rotor
     * rotates.</p>
     *
     * @return non-negative integer that identifies the rotor (type or instance)
     */
    int getId();

    Wire getWire(int row);

    int notchDist();
}