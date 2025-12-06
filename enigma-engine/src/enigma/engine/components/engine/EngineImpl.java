package enigma.engine.components.engine;

import enigma.engine.components.dto.CodeConfigurationDTO;
import enigma.engine.components.dto.MachineDataDTO;
import enigma.engine.components.loader.Loader;
import enigma.machine.component.code.Code;
import enigma.machine.component.machine.Machine;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.ArrayList;
import java.util.List;

public class EngineImpl implements Engine {

    private Machine machine;
    private Loader loader;
    private int processedMessagesCount;
    private CodeConfigurationDTO originalCodeConfiguration;

    @Override
    public void loadXml(String path) {
        processedMessagesCount = 0;
        originalCodeConfiguration = null;
    }

    @Override
    public MachineDataDTO machineData() {
        int availableRotorsCount = loader != null ? loader.getAvailableRotorsCount() : 0;
        int availableReflectorsCount = loader != null ? loader.getAvailableReflectorsCount() : 0;
        
        CodeConfigurationDTO currentConfig = null;
        if (machine != null && machine.getCode() != null) {
            currentConfig = createCodeConfigurationDTO(machine.getCode());
        }
        
        return new MachineDataDTO(
            availableRotorsCount,
            availableReflectorsCount,
            processedMessagesCount,
            originalCodeConfiguration,
            currentConfig
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
            originalCodeConfiguration = createCodeConfigurationDTO(code);
        } else {
            originalCodeConfiguration = null;
        }
    }

    private CodeConfigurationDTO createCodeConfigurationDTO(Code code) {
        List<Rotor> rotors = code.getRotors();
        List<Integer> rotorIds = new ArrayList<>();
        List<Character> rotorPositions = new ArrayList<>();
        
        for (Rotor rotor : rotors) {
            rotorIds.add(rotor.getId());
            rotorPositions.add(rotor.getPosition());
        }
        
        Reflector reflector = code.getReflector();
        String reflectorId = reflector != null ? reflector.getId() : null;
        
        return new CodeConfigurationDTO(rotorIds, rotorPositions, reflectorId);
    }
}
