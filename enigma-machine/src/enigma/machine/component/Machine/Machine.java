package enigma.machine.component.machine;

import enigma.machine.component.code.Code;

/**
 * Represents the Enigma encryption/decryption machine.
 * 
 * <p>The Machine is the main entry point for processing messages.
 * It combines a keyboard, code configuration (rotors + reflector),
 * and lampboard to encrypt or decrypt characters.</p>
 * 
 * <p><strong>Processing Flow:</strong></p>
 * <ol>
 *   <li>Keyboard converts input character to alphabet index</li>
 *   <li>Rotors advance (mechanical stepping)</li>
 *   <li>Signal passes through rotors (forward direction)</li>
 *   <li>Reflector swaps the signal</li>
 *   <li>Signal passes back through rotors (backward direction)</li>
 *   <li>Lampboard converts output index to character</li>
 * </ol>
 */
public interface Machine {
    
    /**
     * Sets the code configuration for the machine.
     * 
     * <p>The code defines which rotors are installed, their initial
     * positions, and which reflector is used.</p>
     * 
     * @param code the code configuration to use
     */
    void setCode(Code code);
    
    /**
     * Processes a single character through the Enigma machine.
     * 
     * <p>This method advances the rotors, then encrypts/decrypts
     * the input character through the full rotor assembly.</p>
     * 
     * @param input the character to process
     * @return the encrypted/decrypted output character
     */
    char process(char input);
}
