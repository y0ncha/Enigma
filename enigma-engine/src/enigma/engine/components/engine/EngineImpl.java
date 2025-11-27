package enigma.engine.components.engine;

import enigma.engine.components.loader.Loader;
import enigma.machine.component.Code.Code;
import enigma.machine.component.Machine.Machine;

public class EngineImpl implements Engine {

    private Machine machine;
    private Loader loader;

    @Override
    public void loadXml(String path) {

    }

    @Override
    public void machineData(String input) {

    }

    @Override
    public void codeManual() {
        Code code = null; // construct code from user input
        machine.setCode(code);
    }

    public void codeRandom() {
        // generate random code
        machine.setCode(null);
    }

    @Override
    public String process(String input) {
        StringBuilder output = new StringBuilder();
        for(char c : input.toCharArray()) {
            output.append(machine.process(c));
        }
        return output.toString();
    }

    @Override
    public void statistics() {

    }
}
