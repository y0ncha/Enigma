package enigma.machine.component.reflector;

/**
 * Represents the reflector component of the Enigma machine.
 * The reflector receives the signal after it passes through all rotors
 * and reflects it back through the rotors in reverse order.
 * A reflector maps each letter to a different letter (no self-mapping).
 */
public interface Reflector {

    /**
     * Processes the input signal by reflecting it to a paired letter.
     *
     * @param input the numeric index of the input letter
     * @return the numeric index of the reflected letter
     */
    int process(int input);
}
