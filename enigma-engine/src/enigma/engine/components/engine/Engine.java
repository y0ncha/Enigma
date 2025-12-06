package enigma.engine.components.engine;

import enigma.shared.dto.MachineState;

public interface Engine {
    void loadXml(String path);
    MachineState machineData();
    void codeManual(/*Args*/);
    void codeRandom();
    String process(String input);
    void statistics();
}
