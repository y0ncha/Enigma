package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

public class MachineImpl implements Machine{
    private Code code;
    private final Keyboard keyboard;

    public MachineImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public void setCode(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

    @Override
    public char process(char input) {
        int intermediate =  keyboard.process(input);
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

    private static int backwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            intermediate = rotors.get(i).process(intermediate, Direction.BACKWARD);
        }
        return intermediate;
    }

    private static int forwardTransform(List<Rotor> rotors, int intermediate) {
        for (int i = 0; i < rotors.size(); i++) {
            intermediate = rotors.get(i).process(intermediate, Direction.FORWARD);
        }
        return intermediate;
    }

    private void advance(List<Rotor> rotors) {
        int rotorIndx = 0;
        boolean shouldAdvance = false;
        do {
            shouldAdvance = rotors.get(rotorIndx).advance();
            rotorIndx++;
        } while (shouldAdvance && rotorIndx < rotors.size());
    }
}
