package enigma.machine.component.rotor;

/**
 * Represents a rotor in the Enigma machine.
 * 
 * <p>A rotor performs character substitution during encryption/decryption.
 * It maintains internal wiring that maps input indices to output indices,
 * and can rotate to change its effective mapping. The rotor's position
 * is indicated by the "window letter" - the letter visible through the
 * machine's window.</p>
 * 
 * <p>Indices used in this interface are global alphabet indices (0-based),
 * where 0 represents the first letter of the alphabet.</p>
 */
public interface Rotor {
    
    /**
     * Processes an input index through the rotor's wiring.
     * 
     * @param input the input index (global alphabet index, 0-based)
     * @param direction FORWARD for right→left traversal, BACKWARD for left→right
     * @return the output index after passing through the rotor's wiring
     */
    int process(int input, Direction direction);
    
    /**
     * Advances the rotor by one position, simulating the mechanical stepping.
     * 
     * <p>This method rotates the rotor by moving the first row to the bottom,
     * mimicking the physical rotation of a real Enigma rotor.</p>
     * 
     * @return true if the notch is engaged (triggers the next rotor to advance),
     *         false otherwise
     */
    boolean advance();
    
    /**
     * Returns the current position of the rotor.
     * 
     * <p>The position is the index of the letter currently visible in the
     * machine's window (the "window letter").</p>
     * 
     * @return the current position as an alphabet index (0-based)
     */
    int getPosition();
    
    /**
     * Returns the current notch index.
     * 
     * <p>The notch determines when this rotor will cause the next rotor
     * to advance. When the notch reaches the window position, the next
     * rotor will be triggered.</p>
     * 
     * @return the current notch index
     */
    int getNotchInd();
    
    /**
     * Sets the rotor to a specific position.
     * 
     * <p>This rotates the rotor until the window letter matches the
     * specified position index.</p>
     * 
     * @param position the target position (alphabet index, 0-based)
     */
    void setPosition(int position);
}
