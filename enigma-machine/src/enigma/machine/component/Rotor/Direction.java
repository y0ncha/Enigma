package enigma.machine.component.rotor;

/**
 * Represents the direction of signal flow through a rotor.
 * <p>
 * In the Enigma machine, signals pass through the rotors twice:
 * first in the forward direction (toward the reflector), then
 * in the backward direction (away from the reflector).
 * </p>
 */
public enum Direction {

    /**
     * Signal flowing toward the reflector (right to left in the rotor bank).
     */
    FORWARD,

    /**
     * Signal flowing away from the reflector (left to right in the rotor bank).
     */
    BACKWARD
}
