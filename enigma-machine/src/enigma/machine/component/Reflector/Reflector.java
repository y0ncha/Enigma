package enigma.machine.component.reflector;

/**
 * Represents a reflector component of the Enigma machine.
 * <p>
 * The reflector receives the signal after it passes through all rotors
 * and sends it back through the rotors in reverse. Unlike rotors, the
 * reflector does not rotate and always maps letters in pairs (if A maps
 * to B, then B maps to A). A letter cannot map to itself.
 * </p>
 */
public interface Reflector {

    /**
     * Processes an input signal through the reflector's wiring.
     * <p>
     * The reflector swaps the input position with its paired position
     * according to the internal wiring configuration.
     * </p>
     *
     * @param input the input signal position (0-based index in the alphabet)
     * @return the reflected signal position
     */
    int process(int input);
}
