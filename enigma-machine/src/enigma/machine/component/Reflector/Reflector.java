package enigma.machine.component.reflector;

/**
 * Represents a reflector in the Enigma machine.
 * 
 * <p>The reflector is a static component that pairs letters together.
 * When a signal enters on one letter, it exits on the paired letter.
 * This creates the symmetric encryption property of the Enigma machine:
 * encrypting a message twice with the same settings yields the original.</p>
 * 
 * <p><strong>Key Properties:</strong></p>
 * <ul>
 *   <li>The reflector does not rotate (unlike rotors)</li>
 *   <li>Each letter is paired with exactly one other letter</li>
 *   <li>No letter can be paired with itself</li>
 *   <li>The mapping is symmetric: if A→B, then B→A</li>
 * </ul>
 * 
 * <p>Indices used in this interface are global alphabet indices (0-based).</p>
 */
public interface Reflector {
    
    /**
     * Processes an input through the reflector.
     * 
     * <p>Returns the paired letter index for the given input index.
     * Due to the symmetric nature of reflector wiring:
     * {@code process(process(x)) == x} for all valid inputs.</p>
     * 
     * @param input the input index (global alphabet index, 0-based)
     * @return the output index (the paired letter)
     */
    int process(int input);
}
