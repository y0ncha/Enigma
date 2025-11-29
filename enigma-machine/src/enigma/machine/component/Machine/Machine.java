package enigma.machine.component.machine;

import enigma.machine.component.code.Code;

public interface Machine {
    void setCode(Code code);
    char process(char input);
}
