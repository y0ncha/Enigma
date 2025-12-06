package enigma.shared.dto.config;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration for creating a Code: chosen rotor ids (left→right), initial positions
 * as characters (left→right), and reflector id.
 *
 * @param rotorIds rotor IDs in user-selected order (left→right)
 * @param initialPositions starting positions as characters (left→right), e.g. ['O','D','X']
 * @param reflectorId reflector identifier (e.g. "I")
 * @since 1.0
 */
public record CodeConfig(
        List<Integer> rotorIds,       // rotor IDs in user-selected order (left → right)
        List<Character> initialPositions, // starting positions as characters (left→right), e.g. ['O','D','X']
        String reflectorId            // e.g. "I"
) {
    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return "<%s><%s><%s>"
                .formatted(
                        rotorIds.toString().replaceAll("[\\[\\] ]", ""),
                        initialPositions == null ? "" : initialPositions.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining()),
                        reflectorId
                );
    }
}
