package enigma.machine.component.Machine;

import enigma.machine.component.Code.Code;

public interface Machine {
    void setCode(Code code);
    char process(char input);
}
