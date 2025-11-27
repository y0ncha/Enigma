package enigma.engine.components.engine;

public interface Engine {
    void loadXml(String path);
    void machineData(String input);
    void codeManual(/*Args*/);
    void codeRandom();
    String process(String input);
    void statistics();
}
