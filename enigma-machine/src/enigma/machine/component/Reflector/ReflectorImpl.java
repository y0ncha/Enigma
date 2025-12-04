package enigma.machine.component.reflector;

import java.util.Map;

/**
 * Implementation of the Enigma reflector.
 * 
 * <p>The reflector is a fixed component in the Enigma machine that pairs
 * letters together. When a signal enters on one letter, it exits on the
 * paired letter, and vice versa. This creates the symmetric encryption
 * property: encrypting twice with the same settings returns the original.</p>
 * 
 * <p><strong>Key Properties:</strong></p>
 * <ul>
 *   <li>The reflector does not rotate (unlike rotors)</li>
 *   <li>Each letter is paired with exactly one other letter</li>
 *   <li>No letter can be paired with itself</li>
 *   <li>The mapping is symmetric: if A→B, then B→A</li>
 * </ul>
 * 
 * <p><strong>Example:</strong></p>
 * <pre>
 *   Reflector mapping: A↔Y, B↔R, C↔U, D↔H, ...
 *   process(0) returns 24 (A→Y)
 *   process(24) returns 0 (Y→A)
 * </pre>
 */
public class ReflectorImpl implements Reflector {
    
    private final int[] mapping;
    
    /**
     * Constructs a reflector with the specified mapping.
     * 
     * @param mapping an array where mapping[i] is the output index for input i
     */
    public ReflectorImpl(int[] mapping) {
        this.mapping = mapping.clone();
    }
    
    /**
     * Constructs a reflector from pair definitions.
     * 
     * @param pairs a map of letter pairs (both directions are automatically set)
     * @param alphabetSize the size of the alphabet
     */
    public ReflectorImpl(Map<Integer, Integer> pairs, int alphabetSize) {
        this.mapping = new int[alphabetSize];
        for (Map.Entry<Integer, Integer> pair : pairs.entrySet()) {
            mapping[pair.getKey()] = pair.getValue();
            mapping[pair.getValue()] = pair.getKey();
        }
    }
    
    /**
     * Processes an input through the reflector.
     * 
     * <p>Returns the paired letter index for the given input index.</p>
     * 
     * @param input the input index (0-based alphabet index)
     * @return the output index (the paired letter)
     */
    @Override
    public int process(int input) {
        return mapping[input];
    }
}
