package enigma.machine.component.machine;

import enigma.machine.component.code.Code;
import enigma.machine.component.keyboard.Keyboard;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Direction;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

public class MachineImpl implements Machine {

    /*--------------- Fields ---------------*/
    private Code code;
    private final Keyboard keyboard;


    /*--------------- Ctor ---------------*/
    public MachineImpl(Keyboard keyboard) {

        this.keyboard = keyboard;
        this.code = null;
    }

    /*--------------- Methods ---------------*/
    @Override
    public void setCode(Code code) {
        this.code = code;
    }

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
    private void advance(List<Rotor> rotors) {
        int rotorIndex = 0;
        boolean shouldAdvance;

        do {
            shouldAdvance = rotors.get(rotorIndex).advance();
            rotorIndex++;
        } while (shouldAdvance && rotorIndex < rotors.size());
    }

    private static int forwardTransform(List<Rotor> rotors, int value) {
        for (int i = 0; i < rotors.size(); i++) {
            value = rotors.get(i).process(value, Direction.FORWARD);
        }
        return value;
    }

    private static int backwardTransform(List<Rotor> rotors, int value) {
        for (int i = rotors.size() - 1; i >= 0; i--) {
            value = rotors.get(i).process(value, Direction.BACKWARD);
        }
        return value;
    }
}