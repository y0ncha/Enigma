package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Implementation of the Enigma machine.
 * Handles the encryption/decryption process by coordinating the keyboard,
 * rotors, and reflector components.
 */
public class MachineImpl implements Machine {

    /** The current code configuration containing rotors and reflector. */
    private Code code;

    /** The keyboard component for input/output character conversion. */
    private final Keyboard keyboard;

    /**
     * Constructs a new MachineImpl with the specified keyboard.
     *
     * @param keyboard the keyboard component for character processing
     */
    public MachineImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void setCode(Code code) {
        this.code = code;
    }

    @Override
    public char process(char input) {
        int intermediate = keyboard.process(input);
        List<Rotor> rotors = code.getRotors();

        // Advance rotors before processing (rightmost always steps)
        advance(rotors);

        // Forward pass through rotors (right to left)
        intermediate = forwardTransform(rotors, intermediate);

        // Reflect the signal
        intermediate = code.getReflector().process(intermediate);

        // Backward pass through rotors (left to right)
        intermediate = backwardTransform(rotors, intermediate);

        return keyboard.lightKey(intermediate);
    }

    /**
     * Transforms the signal backward through all rotors (left to right).
     *
     * @param rotors the list of rotors
     * @param intermediate the current signal value
     * @return the transformed signal value
     */
    private static int backwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            intermediate = rotors.get(i).process(intermediate, Direction.BACKWARD);
        }
        return intermediate;
    }

    /**
     * Transforms the signal forward through all rotors (right to left).
     *
     * @param rotors the list of rotors
     * @param intermediate the current signal value
     * @return the transformed signal value
     */
    private static int forwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = 0; i < rotors.size(); i++) {
            intermediate = rotors.get(i).process(intermediate, Direction.FORWARD);
        }
        return intermediate;
    }

    /**
     * Advances the rotors according to Enigma stepping rules.
     * The rightmost rotor always advances; subsequent rotors advance
     * when the previous rotor's notch is at the window position.
     *
     * @param rotors the list of rotors to advance
     */
    private void advance(List<Rotor> rotors) {
        int rotorIndx = 0;
        boolean shouldAdvance = false;
        do {
            shouldAdvance = rotors.get(rotorIndx).advance();
            rotorIndx++;
        } while (shouldAdvance && rotorIndx < rotors.size());
    }
}
