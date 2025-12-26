package enigma.shared.dto.config;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User-provided code configuration for setting up the Enigma machine.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <h2>Purpose</h2>
 * <p>This record contains all information needed to configure the machine's code:
 * which rotors to use, their starting positions, which reflector to use, and
 * optionally plugboard pairs. It represents the user's configuration choice
 * before validation and machine setup.</p>
 *
 * <h2>Ordering Convention</h2>
 * <p>All fields use <b>left→right</b> ordering matching the user-facing machine
 * window view:</p>
 * <ul>
 *   <li><b>rotorIds:</b> [1, 2, 3] means rotor 1 leftmost, rotor 3 rightmost</li>
 *   <li><b>positions:</b> ['O', 'D', 'X'] means leftmost='O', rightmost='X'</li>
 * </ul>
 *
 * <h2>Position Model</h2>
 * <p>Rotor positions are represented as <b>char</b> values from the alphabet
 * (e.g., 'A', 'B', 'C', 'O', 'D', 'X'). This matches the visual window
 * letters visible on a physical Enigma machine and the characters users see.</p>
 *
 * <h2>Plugboard (Exercise 2)</h2>
 * <p>The plugboard field contains character pairs for plugboard wiring:</p>
 * <ul>
 *   <li>Even-length string: "ABCD" means A↔B, C↔D</li>
 *   <li>Empty string "" means no plugboard connections</li>
 *   <li>Each character appears at most once</li>
 *   <li>No self-mappings (e.g., "AA" is invalid)</li>
 * </ul>
 *
 * <h2>Validation</h2>
 * <p>This DTO is a data container only. Validation is performed by:</p>
 * <ul>
 *   <li><b>Console:</b> Format validation (parsing, A-Z checks, length matching)</li>
 *   <li><b>Engine:</b> Semantic validation (rotor existence, alphabet membership)</li>
 * </ul>
 *
 * <h2>String Format</h2>
 * <p>toString() produces compact config format: {@code <1,2,3><ODX><I>}</p>
 * <ul>
 *   <li>First group: rotor IDs (left→right)</li>
 *   <li>Second group: positions as chars (left→right)</li>
 *   <li>Third group: reflector ID</li>
 *   <li>Plugboard not shown (for brevity)</li>
 * </ul>
 *
 * <h2>Immutability</h2>
 * <p>As a record, this class is immutable. Lists are copied by the record
 * constructor, ensuring the configuration cannot be modified after creation.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Create manual configuration
 * CodeConfig config = new CodeConfig(
 *     List.of(1, 2, 3),           // rotors: 1=left, 3=right
 *     List.of('O', 'D', 'X'),     // positions: O=left, X=right
 *     "I",                        // reflector
 *     ""                          // no plugboard
 * );
 *
 * // Pass to engine for validation and application
 * engine.configManual(config);
 * </pre>
 *
 * @param rotorIds rotor IDs in left→right order (e.g., [1, 2, 3])
 * @param positions starting positions as characters in left→right order (e.g., ['O','D','X'])
 * @param reflectorId reflector identifier (e.g., "I", "II")
 * @since 1.0
 */
public record CodeConfig(
        List<Integer> rotorIds,       // rotor IDs in user-selected order (left → right)
        List<Character> positions,    // starting positions as characters (left→right), e.g. ['O','D','X']
        String reflectorId,           // e.g. "I"
        String plugboard              // plugboard pairs (e.g., "ABCD" = A↔B, C↔D), "" = none
) {
    /**
     * Returns a compact string representation of the configuration.
     *
     * <p>Format: {@code <rotorIds><positions><reflectorId>}</p>
     * <p>Example: {@code <1,2,3><ODX><I>}</p>
     *
     * <p>Note: Plugboard is not included in the string representation for brevity.</p>
     *
     * @return compact config string
     */
    @Override
    public String toString() {

        // Format rotor IDs as comma-separated string without brackets/spaces
        String rotorStr = rotorIds().toString().replaceAll("[\\[\\] ]", "");

        // Format positions as concatenated string of characters
        String positionStr = positions() == null ? "" :
                positions()
                        .stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining());

        // Reflector ID or empty if null
        String reflectorStr = reflectorId() == null ? "" : reflectorId();

        // Build final string
        StringBuilder result = new StringBuilder();
        result.append("<").append(rotorStr).append(">")
                .append("<").append(positionStr).append(">")
                .append("<").append(reflectorStr).append(">");

        // Optionally include plugboard if configured
        if (plugboard != null && !plugboard.isEmpty()) {
            result.append("<").append(formatPlugboard()).append(">");
        }

        return result.toString();
    }

    /** Formats the plugboard string into pairs separated by commas.
     * Example: "ABCD" -> "A|B,C|D"
     * @return formatted plugboard string
     */
    private String formatPlugboard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plugboard.length(); i += 2) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(plugboard.charAt(i))
                    .append("|")
                    .append(plugboard.charAt(i + 1));
        }
        return sb.toString();
    }
}
