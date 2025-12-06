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
    private int processedMessagesCount;
    private List<Integer> originalRotorIds;
    private List<Character> originalRotorPositions;
    private String originalReflectorId;

    @Override
    public void loadXml(String path) {
        processedMessagesCount = 0;
        originalRotorIds = null;
        originalRotorPositions = null;
        originalReflectorId = null;
    }

    @Override
    public MachineState machineData() {
        int availableRotorsCount = loader != null ? loader.getAvailableRotorsCount() : 0;
        int availableReflectorsCount = loader != null ? loader.getAvailableReflectorsCount() : 0;
        
        CodeData currentData = extractCodeData(machine != null ? machine.getCode() : null);
        
        return new MachineState(
            availableRotorsCount,
            availableReflectorsCount,
            processedMessagesCount,
            copyList(originalRotorIds),
            copyList(originalRotorPositions),
            originalReflectorId,
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
        processedMessagesCount++;
        return output.toString();
    }

    @Override
    public void statistics() {

    }

    private void setCodeAndTrackOriginal(Code code) {
        machine.setCode(code);
        CodeData data = extractCodeData(code);
        originalRotorIds = data.rotorIds;
        originalRotorPositions = data.rotorPositions;
        originalReflectorId = data.reflectorId;
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
