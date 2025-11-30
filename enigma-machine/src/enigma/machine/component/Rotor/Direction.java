package enigma.machine.component.rotor;

/**
 * Represents the direction of signal flow through a rotor.
 * FORWARD is the initial pass from keyboard to reflector.
 * BACKWARD is the return pass from reflector to output.
 */
public enum Direction {
    /** Signal flowing from keyboard toward reflector. */
    FORWARD,

    /** Signal flowing from reflector back toward keyboard. */
    BACKWARD
}
