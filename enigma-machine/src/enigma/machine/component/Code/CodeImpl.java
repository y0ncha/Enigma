package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the Enigma code configuration.
 * 
 * <p>A Code represents a complete configuration of rotors and reflector
 * for the Enigma machine. It contains:</p>
 * <ul>
 *   <li>An ordered list of rotors (stored in machine order: right to left)</li>
 *   <li>A reflector</li>
 * </ul>
 * 
 * <p><strong>Rotor Order:</strong></p>
 * <p>Rotors are stored in the order they appear in the machine, from right
 * (closest to keyboard) to left (closest to reflector). Index 0 is the
 * rightmost (fastest) rotor, and higher indices are progressively to the left.</p>
 * 
 * <p>This order matches the signal flow direction: signals enter from the
 * keyboard (right side) and travel through rotors toward the reflector (left).</p>
 */
public class CodeImpl implements Code {
    
    private final List<Rotor> rotors;
    private final Reflector reflector;
    
    /**
     * Constructs a code configuration with the specified rotors and reflector.
     * 
     * @param rotors the list of rotors in machine order (right to left, index 0 = rightmost)
     * @param reflector the reflector
     */
    public CodeImpl(List<Rotor> rotors, Reflector reflector) {
        this.rotors = new ArrayList<>(rotors);
        this.reflector = reflector;
    }
    
    /**
     * Returns the list of rotors.
     * 
     * <p>The returned list is unmodifiable. Rotors are in machine order:
     * index 0 is the rightmost (fastest) rotor.</p>
     * 
     * @return an unmodifiable list of rotors
     */
    @Override
    public List<Rotor> getRotors() {
        return Collections.unmodifiableList(rotors);
    }
    
    /**
     * Returns the reflector.
     * 
     * @return the reflector
     */
    @Override
    public Reflector getReflector() {
        return reflector;
    }
    
    /**
     * Returns the current positions of all rotors.
     * 
     * <p>The positions are in the same order as the rotors list (right to left).
     * Each position is the alphabet index of the letter currently visible
     * in that rotor's window.</p>
     * 
     * @return a list of current rotor positions
     */
    @Override
    public List<Integer> getPositions() {
        List<Integer> positions = new ArrayList<>();
        for (Rotor rotor : rotors) {
            positions.add(rotor.getPosition());
        }
        return positions;
    }
}
