package enigma.machine.machine;

import enigma.machine.code.Code;
import enigma.machine.keyboard.Keyboard;
import enigma.machine.rotor.Direction;
import enigma.machine.rotor.Rotor;

import java.util.List;

/**
 * Default {@link Machine} implementation that coordinates rotor stepping,
 * forward/backward transformations and reflector processing.
 *
 * @since 1.0
 */
public class MachineImpl implements Machine {

    /*--------------- Fields ---------------*/
    private Code code;
    private final Keyboard keyboard;


    /*--------------- Ctor ---------------*/
    /**
     * Construct a machine with a provided {@link Keyboard}.
     *
     * @param keyboard keyboard adapter used for char/index conversions
     * @since 1.0
     */
    public MachineImpl(Keyboard keyboard) {

        this.keyboard = keyboard;
        this.code = null;
    }

    /*--------------- Methods ---------------*/
    /**
     * {@inheritDoc}
     */
    @Override
    public void setCode(Code code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char process(char input) {

        if (code == null) {
            throw new IllegalStateException("Machine is not configured with a code");
        }

        int intermediate = keyboard.process(input);
        List<Rotor> rotors = code.getRotors();

        advance(rotors);
        intermediate = forwardTransform(rotors, intermediate);
        intermediate = code.getReflector().process(intermediate);
        intermediate = backwardTransform(rotors, intermediate);

        return keyboard.lightKey(intermediate);
    }

    /*--------------- Helpers ---------------*/
    /**
     * Advance rotors starting from the rightmost rotor.
     *
     * @param rotors list of rotors in right→left order
     * @since 1.0
     */
    private void advance(List<Rotor> rotors) {
        int rotorIndex = 0;
        boolean shouldAdvance;

        do {
            shouldAdvance = rotors.get(rotorIndex).advance();
            rotorIndex++;
        } while (shouldAdvance && rotorIndex < rotors.size());
    }

    /**
     * Apply forward transformation through rotors (right→left).
     *
     * @param rotors rotors list
     * @param value input index
     * @return transformed index after forward pass
     * @since 1.0
     */
    private static int forwardTransform(List<Rotor> rotors, int value) {
        for (int i = 0; i < rotors.size(); i++) {
            value = rotors.get(i).process(value, Direction.FORWARD);
        }
        return value;
    }

    /**
     * Apply backward transformation through rotors (left→right).
     *
     * @param rotors rotors list
     * @param value input index
     * @return transformed index after backward pass
     * @since 1.0
     */
    private static int backwardTransform(List<Rotor> rotors, int value) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            value = rotors.get(i).process(value, Direction.BACKWARD);
        }
        return value;
    }
}