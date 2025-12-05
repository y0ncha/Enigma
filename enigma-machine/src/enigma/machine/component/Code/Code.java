package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Represents a complete code configuration for the Enigma machine.
 * 
 * <p>A Code encapsulates all the settings needed to configure the Enigma
 * machine for encryption/decryption:</p>
 * <ul>
 *   <li>An ordered list of rotors (in machine order: right to left)</li>
 *   <li>A reflector</li>
 * </ul>
 * 
 * <p><strong>Rotor Order:</strong></p>
 * <p>Rotors are ordered from right (closest to keyboard) to left (closest
 * to reflector). Index 0 is the rightmost (fastest stepping) rotor.</p>
 * 
 * <p><strong>Position Representation:</strong></p>
 * <p>Positions are represented as alphabet indices (0-based), where 0
 * represents the first letter of the alphabet.</p>
 */
public interface Code {
    
    /**
     * Returns the list of rotors in this configuration.
     * 
     * <p>Rotors are in machine order: index 0 is the rightmost rotor
     * (closest to keyboard, steps most frequently).</p>
     * 
     * @return the list of rotors
     */
    List<Rotor> getRotors();
    
    /**
     * Returns the reflector in this configuration.
     * 
     * @return the reflector
     */
    Reflector getReflector();
    
    /**
     * Returns the current positions of all rotors.
     * 
     * <p>Positions are in the same order as the rotors (right to left).
     * Each position is the alphabet index of the letter currently
     * visible in that rotor's window.</p>
     * 
     * @return a list of current rotor positions (alphabet indices)
     */
    List<Integer> getPositions();
}
