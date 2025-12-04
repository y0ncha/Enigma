package enigma.machine.factory;

import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Factory interface for creating rotors.
 * 
 * <p>Implementations of this interface are responsible for constructing
 * {@link Rotor} instances from specification data (e.g., XML configuration).</p>
 */
public interface RotorFactory {
    
    /**
     * Creates a rotor from the given wiring specification.
     * 
     * @param rightColumn the right column values (entry contacts)
     * @param leftColumn the left column values (exit contacts)
     * @param notchIndex the notch position index
     * @param startPosition the initial position to set the rotor to
     * @return a configured Rotor instance
     */
    Rotor create(List<Integer> rightColumn, List<Integer> leftColumn, 
                 int notchIndex, int startPosition);
}
