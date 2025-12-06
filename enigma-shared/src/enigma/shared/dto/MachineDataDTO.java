package enigma.shared.dto;

import java.util.List;

/**
 * Machine state snapshot.
 */
public record MachineDataDTO(
        int availableRotorsCount,
        int availableReflectorsCount,
        int processedMessagesCount,
        List<Integer> originalRotorIds,
        List<Character> originalRotorPositions,
        String originalReflectorId,
        List<Integer> currentRotorIds,
        List<Character> currentRotorPositions,
        String currentReflectorId
) {
}
