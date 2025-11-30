package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Represents the code configuration for the Enigma machine.
 * A code configuration includes the set of rotors, their initial positions,
 * and the reflector used for encryption/decryption.
 */
public interface Code {

    /**
     * Returns the list of rotors in the current configuration.
     * The order is right to left (rightmost rotor first).
     *
     * @return the list of rotors
     */
    List<Rotor> getRotors();

    /**
     * Returns the reflector used in this configuration.
     *
     * @return the reflector
     */
    Reflector getReflector();

    /**
     * Returns the initial positions of the rotors.
     *
     * @return the list of rotor positions
     */
    List<Integer> getPositions();
}
