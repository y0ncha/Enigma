package enigma.machine.component.rotor;

/**
 * Represents the direction of signal flow through a rotor.
 * 
 * <p>In an Enigma machine, signals pass through each rotor twice:</p>
 * <ul>
 *   <li>FORWARD: Signal travels from keyboard toward reflector (right→left)</li>
 *   <li>BACKWARD: Signal returns from reflector toward lampboard (left→right)</li>
 * </ul>
 * 
 * <p>The rotor wiring produces different substitutions depending on
 * which direction the signal is traveling.</p>
 */
public enum Direction {
    /**
     * Forward direction: signal flows from keyboard toward reflector.
     * In the mechanical model, this maps from right column to left column.
     */
    FORWARD,
    
    /**
     * Backward direction: signal returns from reflector toward lampboard.
     * In the mechanical model, this maps from left column to right column.
     */
    BACKWARD
}
