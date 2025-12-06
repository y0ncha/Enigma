package enigma.engine.components.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a code configuration snapshot.
 */
public class CodeConfigurationDTO {
    private final List<Integer> rotorIds;
    private final List<Character> rotorPositions;
    private final String reflectorId;

    public CodeConfigurationDTO(List<Integer> rotorIds, List<Character> rotorPositions, String reflectorId) {
        if (rotorIds.size() != rotorPositions.size()) {
            throw new IllegalArgumentException("Rotor IDs and positions must have the same size");
        }
        this.rotorIds = new ArrayList<>(rotorIds);
        this.rotorPositions = new ArrayList<>(rotorPositions);
        this.reflectorId = reflectorId;
    }

    public List<Integer> getRotorIds() {
        return Collections.unmodifiableList(rotorIds);
    }

    public List<Character> getRotorPositions() {
        return Collections.unmodifiableList(rotorPositions);
    }

    public String getReflectorId() {
        return reflectorId;
    }
}
