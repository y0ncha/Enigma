package enigma.machine.component.Machine;

import enigma.machine.component.Code.Code;
import enigma.machine.component.Rotor.Direction;
import enigma.machine.component.Keyboard.Keyboard;
import enigma.machine.component.Rotor.Rotor;

import java.util.List;

public class MachineImpl {
    private Code code;
    private final Keyboard keyboard;

    public MachineImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public char process(char input) {
        int intermediate =  keyboard.process(input);

        // forward
        List<Rotor> rotors = code.getRotors();

        for (int i = 0; i < rotors.size(); i++) {
            intermediate = rotors.get(i).process(intermediate, Direction.FORWARD);
        }

        // reflect
        intermediate = code.getReflector().process(intermediate);


        // backward
        for (int i = rotors.size() - 1; i >= 0; i--) {
            intermediate = rotors.get(i).process(intermediate, Direction.BACKWARD);
        }

        char res = keyboard.lightKey(intermediate);
        return res;
    }
}
