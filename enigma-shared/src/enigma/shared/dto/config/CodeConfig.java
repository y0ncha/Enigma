package enigma.shared.dto.config;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration for creating a Code: chosen rotor IDs (left→right), initial positions
 * as characters (left→right), and reflector ID.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <h2>Ordering Convention</h2>
 * <p>All fields use <b>left→right</b> ordering matching the user-facing machine
 * window view:</p>
 * <ul>
 *   <li><b>rotorIds:</b> [1, 2, 3] means rotor 1 leftmost, rotor 3 rightmost</li>
 *   <li><b>initialPositions:</b> ['O', 'D', 'X'] means leftmost='O', rightmost='X'</li>
 * </ul>
 *
 * <h2>Position Model</h2>
 * <p>Rotor positions are represented as <b>char</b> values from the alphabet
 * (e.g., 'A', 'B', 'C', 'O', 'D', 'X'). This matches the visual window
 * letters visible on a physical Enigma machine.</p>
 *
 * <h2>String Format</h2>
 * <p>toString() produces config format: {@code <1,2,3><ODX><I>}</p>
 * <ul>
 *   <li>First group: rotor IDs (left→right)</li>
 *   <li>Second group: positions as chars (left→right)</li>
 *   <li>Third group: reflector ID</li>
 * </ul>
 *
 * @param rotorIds rotor IDs in left→right order (e.g., [1, 2, 3])
 * @param positions starting positions as characters in left→right order (e.g., ['O','D','X'])
 * @param reflectorId reflector identifier (e.g., "I", "II")
 * @since 1.0
 */
public record CodeConfig(
        List<Integer> rotorIds,       // rotor IDs in user-selected order (left → right)
        List<Character> positions, // starting positions as characters (left→right), e.g. ['O','D','X']
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
                        positions == null ? "" : positions.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining()),
                        reflectorId
                );
    }
}
