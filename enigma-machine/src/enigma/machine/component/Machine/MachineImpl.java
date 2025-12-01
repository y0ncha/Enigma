package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Implementation of the Enigma machine.
 * <p>
 * This class simulates the encryption/decryption process of an Enigma machine,
 * processing characters through the keyboard, rotors, and reflector.
 * </p>
 */
public class MachineImpl implements Machine {

    /** The current code configuration (rotors and reflector). */
    private Code code;

    /** The keyboard component for input/output character mapping. */
    private final Keyboard keyboard;

    /**
     * Constructs a new MachineImpl with the specified keyboard.
     *
     * @param keyboard the keyboard component for character I/O mapping
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
     * <p>
     * The processing follows the Enigma machine signal path:
     * <ol>
     *   <li>Keyboard input conversion</li>
     *   <li>Rotor advancement (stepping mechanism)</li>
     *   <li>Forward pass through rotors (right to left)</li>
     *   <li>Reflection through the reflector</li>
     *   <li>Backward pass through rotors (left to right)</li>
     *   <li>Keyboard output conversion</li>
     * </ol>
     * </p>
     */
    @Override
    public char process(char input) {
        int intermediate = keyboard.process(input);
        List<Rotor> rotors = code.getRotors();

        // advance
        advance(rotors);

        // forward
        intermediate = forwardTransform(rotors, intermediate);

        // reflect
        intermediate = code.getReflector().process(intermediate);

        // backward
        intermediate = backwardTransform(rotors, intermediate);

        return keyboard.lightKey(intermediate);
    }

    /**
     * Performs the backward transformation through the rotors.
     * <p>
     * Passes the signal through all rotors from left to right (reverse order).
     * </p>
     *
     * @param rotors       the list of rotors to pass through
     * @param intermediate the current signal position
     * @return the transformed signal position
     */
    private static int backwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            intermediate = rotors.get(i).process(intermediate, Direction.BACKWARD);
        }
        return intermediate;
    }

    /**
     * Performs the forward transformation through the rotors.
     * <p>
     * Passes the signal through all rotors from right to left (index order).
     * </p>
     *
     * @param rotors       the list of rotors to pass through
     * @param intermediate the current signal position
     * @return the transformed signal position
     */
    private static int forwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = 0; i < rotors.size(); i++) {
            intermediate = rotors.get(i).process(intermediate, Direction.FORWARD);
        }
        return intermediate;
    }

    /**
     * Advances the rotors according to the Enigma stepping mechanism.
     * <p>
     * The rightmost rotor always advances. Additional rotors advance
     * when the previous rotor passes its notch position (turnover).
     * </p>
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
