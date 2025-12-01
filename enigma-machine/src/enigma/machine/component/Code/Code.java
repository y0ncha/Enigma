package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Represents the complete code configuration for the Enigma machine.
 * <p>
 * A code configuration includes the selected rotors, their initial positions,
 * and the reflector. This configuration determines how the machine encrypts
 * and decrypts messages. Rotors are ordered from right to left (index 0 is
 * the rightmost rotor).
 * </p>
 */
public interface Code {

    /**
     * Returns the list of rotors in the current configuration.
     * <p>
     * The rotors are ordered from right to left in the machine, meaning
     * index 0 is the rightmost rotor (the one that advances with every keypress).
     * </p>
     *
     * @return an unmodifiable list of rotors in right-to-left order
     */
    List<Rotor> getRotors();

    /**
     * Returns the reflector in the current configuration.
     *
     * @return the reflector used by the machine
     */
    Reflector getReflector();

    /**
     * Returns the current positions of all rotors.
     * <p>
     * Each position is a 0-based index representing the offset from
     * the rotor's initial position. Positions are in the same order
     * as the rotors (right to left).
     * </p>
     *
     * @return a list of rotor positions corresponding to each rotor
     */
    List<Integer> getPositions();
}
