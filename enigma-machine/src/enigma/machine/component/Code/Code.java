package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Represents a machine code configuration: rotors, reflector and metadata.
 *
 * Implementations are expected to provide ordered rotor instances (right→left)
 * together with their configuration metadata.
 *
 * @since 1.0
 */
public interface Code {

    // Components
    /**
     * Active rotors in right-to-left order.
     *
     * @return ordered list of {@link Rotor}
     */
    List<Rotor> getRotors();

    /**
     * Active reflector for the current code.
     *
     * @return reflector instance
     */
    Reflector getReflector();

    // Metadata
    /**
     * Rotor start positions (0-based) for each rotor (right→left).
     *
     * @return list of numeric rotor positions
     */
    List<Integer> getPositions();

    /**
     * Rotor ids corresponding to the rotors list (right→left).
     *
     * @return list of rotor ids
     */
    List<Integer> getRotorIds();

    /**
     * Reflector identifier (e.g. "I", "II").
     *
     * @return reflector id string
     */
    String getReflectorId();
}
