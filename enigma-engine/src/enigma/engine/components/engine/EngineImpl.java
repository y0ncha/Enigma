package enigma.engine.components.engine;

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
    private Code originalCode;

    @Override
    public void loadXml(String path) {
        messagesCount = 0;
        originalCode = null;
    }

    @Override
    public MachineState getState() {
        int rotorsCount = loader != null ? loader.getAvailableRotorsCount() : 0;
        int reflectorsCount = loader != null ? loader.getAvailableReflectorsCount() : 0;
        
        CodeData originalData = extractCodeData(originalCode);
        CodeData currentData = extractCodeData(machine != null ? machine.getCode() : null);
        
        return new MachineState(
            rotorsCount,
            reflectorsCount,
            messagesCount,
            copyList(originalData.rotorIds),
            copyList(originalData.rotorPositions),
            originalData.reflectorId,
            copyList(currentData.rotorIds),
            copyList(currentData.rotorPositions),
            currentData.reflectorId
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
        originalCode = code;
    }

    private CodeData extractCodeData(Code code) {
        if (code == null) {
            return new CodeData(null, null, null);
        }
        
        List<Integer> rotorIds = new ArrayList<>();
        List<Character> rotorPositions = new ArrayList<>();
        
        for (Rotor rotor : code.getRotors()) {
            rotorIds.add(rotor.getId());
            rotorPositions.add(rotor.getPosition());
        }
        
        Reflector reflector = code.getReflector();
        String reflectorId = reflector != null ? reflector.getId() : null;
        
        return new CodeData(rotorIds, rotorPositions, reflectorId);
    }

    private <T> List<T> copyList(List<T> list) {
        return list != null ? Collections.unmodifiableList(new ArrayList<>(list)) : null;
    }

    private static class CodeData {
        final List<Integer> rotorIds;
        final List<Character> rotorPositions;
        final String reflectorId;

        CodeData(List<Integer> rotorIds, List<Character> rotorPositions, String reflectorId) {
            this.rotorIds = rotorIds;
            this.rotorPositions = rotorPositions;
            this.reflectorId = reflectorId;
        }
    }
}
