package enigma.engine.components.engine;

import enigma.engine.components.config.CodeConfig;
import enigma.engine.components.loader.Loader;
import enigma.machine.component.code.Code;
import enigma.machine.component.machine.Machine;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;
import enigma.shared.dto.MachineState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EngineImpl implements Engine {

    private Machine machine;
    private Loader loader;
    private int messagesCount;
    private CodeConfig originalCodeConfig;

    @Override
    public void loadXml(String path) {
        messagesCount = 0;
        originalCodeConfig = null;
    }

    @Override
    public MachineState getState() {
        int rotorsCount = loader != null ? loader.getAvailableRotorsCount() : 0;
        int reflectorsCount = loader != null ? loader.getAvailableReflectorsCount() : 0;
        
        CodeConfig currentCodeConfig = extractCodeConfig(machine != null ? machine.getCode() : null);
        
        return new MachineState(
            rotorsCount,
            reflectorsCount,
            messagesCount,
            copyList(originalCodeConfig != null ? originalCodeConfig.rotorIds() : null),
            copyList(originalCodeConfig != null ? originalCodeConfig.rotorPositions() : null),
            originalCodeConfig != null ? originalCodeConfig.reflectorId() : null,
            copyList(currentCodeConfig != null ? currentCodeConfig.rotorIds() : null),
            copyList(currentCodeConfig != null ? currentCodeConfig.rotorPositions() : null),
            currentCodeConfig != null ? currentCodeConfig.reflectorId() : null
        );
    }

    @Override
    public void codeManual() {
        Code code = null; // construct code from user input
        setCodeAndTrackOriginal(code);
    }

    @Override
    public void codeRandom() {
        // generate random code
        Code code = null;
        setCodeAndTrackOriginal(code);
    }

    @Override
    public String process(String input) {
        StringBuilder output = new StringBuilder();
        for(char c : input.toCharArray()) {
            output.append(machine.process(c));
        }
        messagesCount++;
        return output.toString();
    }

    @Override
    public void statistics() {

    }

    private void setCodeAndTrackOriginal(Code code) {
        machine.setCode(code);
        originalCodeConfig = extractCodeConfig(code);
    }

    private CodeConfig extractCodeConfig(Code code) {
        if (code == null) {
            return null;
        }
        
        List<Integer> rotorIds = new ArrayList<>();
        List<Character> rotorPositions = new ArrayList<>();
        
        for (Rotor rotor : code.getRotors()) {
            rotorIds.add(rotor.getId());
            rotorPositions.add(rotor.getPosition());
        }
        
        Reflector reflector = code.getReflector();
        String reflectorId = reflector != null ? reflector.getId() : null;
        
        return new CodeConfig(rotorIds, rotorPositions, reflectorId);
    }

    private <T> List<T> copyList(List<T> list) {
        return list != null ? Collections.unmodifiableList(new ArrayList<>(list)) : null;
    }
}
