package enigma.engine.components.dto;

import java.util.List;

/**
 * Represents a code configuration snapshot.
 */
public class CodeConfigurationDTO {
    private final List<Integer> rotorIds;
    private final List<Character> rotorPositions;
    private final String reflectorId;

    public CodeConfigurationDTO(List<Integer> rotorIds, List<Character> rotorPositions, String reflectorId) {
        this.rotorIds = rotorIds;
        this.rotorPositions = rotorPositions;
        this.reflectorId = reflectorId;
    }

    public List<Integer> getRotorIds() {
        return rotorIds;
    }

    public List<Character> getRotorPositions() {
        return rotorPositions;
    }

    public String getReflectorId() {
        return reflectorId;
    }
}
