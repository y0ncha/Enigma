package enigma.engine.components.engine;

import enigma.engine.components.loader.Loader;
import enigma.machine.component.code.Code;
import enigma.machine.component.machine.Machine;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;
import enigma.shared.dto.MachineDataDTO;

import java.util.ArrayList;
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
    public MachineDataDTO machineData() {
        int availableRotorsCount = loader != null ? loader.getAvailableRotorsCount() : 0;
        int availableReflectorsCount = loader != null ? loader.getAvailableReflectorsCount() : 0;
        
        List<Integer> currentRotorIds = null;
        List<Character> currentRotorPositions = null;
        String currentReflectorId = null;
        
        if (machine != null && machine.getCode() != null) {
            Code currentCode = machine.getCode();
            currentRotorIds = new ArrayList<>();
            currentRotorPositions = new ArrayList<>();
            
            for (Rotor rotor : currentCode.getRotors()) {
                currentRotorIds.add(rotor.getId());
                currentRotorPositions.add(rotor.getPosition());
            }
            
            Reflector reflector = currentCode.getReflector();
            currentReflectorId = reflector != null ? reflector.getId() : null;
        }
        
        return new MachineDataDTO(
            availableRotorsCount,
            availableReflectorsCount,
            processedMessagesCount,
            originalRotorIds,
            originalRotorPositions,
            originalReflectorId,
            currentRotorIds,
            currentRotorPositions,
            currentReflectorId
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
        if (code != null) {
            originalRotorIds = new ArrayList<>();
            originalRotorPositions = new ArrayList<>();
            
            for (Rotor rotor : code.getRotors()) {
                originalRotorIds.add(rotor.getId());
                originalRotorPositions.add(rotor.getPosition());
            }
            
            Reflector reflector = code.getReflector();
            originalReflectorId = reflector != null ? reflector.getId() : null;
        } else {
            originalRotorIds = null;
            originalRotorPositions = null;
            originalReflectorId = null;
        }
    }
}
