package enigma.shared.dto.config;

import java.util.List;

/**
 * Configuration for creating a Code: chosen rotor ids (left→right), initial positions
 * as numeric indices (left→right), and reflector id.
 *
 * @param rotorIds rotor IDs in user-selected order (left→right)
 * @param initialPositions numeric positions left→right (0-based indices)
 * @param reflectorId reflector identifier (e.g. "I")
 * @since 1.0
 */
public record CodeConfig(
        List<Integer> rotorIds,       // rotor IDs in user-selected order (left → right)
        List<Integer> initialPositions, // numeric positions left→right (0-based indices)
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
                        initialPositions.stream()
                                .map(i -> String.valueOf((char)('A' + i)))
                                .reduce("", String::concat),
                        reflectorId
                );
    }
}
