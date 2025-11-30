package enigma.engine.components.dto;

import java.util.List;

/**
 * Configuration for creating a Code: chosen rotor ids (left→right), initial positions
 * as numeric indices (left→right), reflector id and plugboard pairs string.
 */
public record CodeConfig(
        List<Integer> rotorIds,       // rotor IDs in user-selected order (left → right)
        List<Integer> initialPositions, // numeric positions left→right (0-based indices)
        String reflectorId            // e.g. "I"
) {}