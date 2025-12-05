package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Implementation of the Enigma encryption/decryption machine.
 * 
 * <p>This class orchestrates the complete encryption/decryption process
 * by coordinating the keyboard, rotors, reflector, and lampboard.</p>
 * 
 * <p><strong>Processing Flow for each character:</strong></p>
 * <ol>
 *   <li>Keyboard converts input character to alphabet index</li>
 *   <li>Rotors advance (rightmost always steps, others step based on notch)</li>
 *   <li>Signal passes forward through rotors (right→left)</li>
 *   <li>Reflector swaps the signal</li>
 *   <li>Signal passes backward through rotors (left→right)</li>
 *   <li>Lampboard converts output index back to character</li>
 * </ol>
 * 
 * <p><strong>Rotor Stepping:</strong></p>
 * <p>Before each character is processed, the rotors advance. The rightmost
 * rotor always advances. When a rotor's notch is at the window position,
 * it causes the next rotor (to its left) to also advance.</p>
 */
public class MachineImpl implements Machine {
    private Code code;
    private final Keyboard keyboard;

    /**
     * Constructs a machine with the specified keyboard.
     * 
     * @param keyboard the keyboard/lampboard for character conversion
     */
    public MachineImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCode(Code code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>The processing order is:</p>
     * <ol>
     *   <li>Convert input character to index via keyboard</li>
     *   <li>Advance rotors (with cascade logic)</li>
     *   <li>Process forward through all rotors</li>
     *   <li>Process through reflector</li>
     *   <li>Process backward through all rotors</li>
     *   <li>Convert output index to character via lampboard</li>
     * </ol>
     */
    @Override
    public char process(char input) {
        int intermediate = keyboard.process(input);
        List<Rotor> rotors = code.getRotors();

        // Advance rotors before processing
        advance(rotors);

        // Forward pass through rotors (right to left)
        intermediate = forwardTransform(rotors, intermediate);

        // Reflect
        intermediate = code.getReflector().process(intermediate);

        // Backward pass through rotors (left to right)
        intermediate = backwardTransform(rotors, intermediate);

        return keyboard.lightKey(intermediate);
    }

    /**
     * Processes signal backward through the rotor stack (left to right).
     * 
     * @param rotors the list of rotors
     * @param intermediate the current signal index
     * @return the signal after backward transformation
     */
    private static int backwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            intermediate = rotors.get(i).process(intermediate, Direction.BACKWARD);
        }
        return intermediate;
    }

    /**
     * Processes signal forward through the rotor stack (right to left).
     * 
     * @param rotors the list of rotors
     * @param intermediate the current signal index
     * @return the signal after forward transformation
     */
    private static int forwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = 0; i < rotors.size(); i++) {
            intermediate = rotors.get(i).process(intermediate, Direction.FORWARD);
        }
        return intermediate;
    }

    /**
     * Advances the rotors with cascade logic.
     * 
     * <p>The rightmost rotor (index 0) always advances. If its advance()
     * returns true (notch engaged), the next rotor also advances, and
     * so on through the rotor stack.</p>
     * 
     * @param rotors the list of rotors to advance
     */
    private void advance(List<Rotor> rotors) {
        int rotorIndex = 0;
        boolean shouldAdvance;
        do {
            shouldAdvance = rotors.get(rotorIndex).advance();
            rotorIndex++;
        } while (shouldAdvance && rotorIndex < rotors.size());
    }
}
