package enigma.engine.components.engine;

import enigma.shared.dto.MachineDataDTO;

public interface Engine {
    void loadXml(String path);
    MachineDataDTO machineData();
    void codeManual(/*Args*/);
    void codeRandom();
    String process(String input);
    void statistics();
}
