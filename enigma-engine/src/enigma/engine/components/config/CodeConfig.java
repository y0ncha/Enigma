package enigma.engine.components.config;

import java.util.List;

/**
 * Lightweight code configuration holding IDs and positions.
 */
public record CodeConfig(
        List<Integer> rotorIds,
        List<Character> rotorPositions,
        String reflectorId
) {
}
