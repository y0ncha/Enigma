package enigma.shared.dto;

import java.util.List;

/**
 * Immutable machine state snapshot.
 */
public record MachineState(
        int rotorsCount,
        int reflectorsCount,
        int messagesCount,
        List<Integer> originalRotorIds,
        List<Character> originalRotorPositions,
        String originalReflectorId,
        List<Integer> currentRotorIds,
        List<Character> currentRotorPositions,
        String currentReflectorId
) {
}
