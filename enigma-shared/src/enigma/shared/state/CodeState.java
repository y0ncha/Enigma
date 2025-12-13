package enigma.shared.state;

import enigma.shared.dto.config.CodeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of code state at a specific moment in time.
 *
 * <p><b>Module:</b> enigma-shared (state snapshots)</p>
 *
 * <h2>Purpose</h2>
 * <p>CodeState captures the complete configuration and current positions of the
 * machine at a point in time. It differs from {@link enigma.shared.dto.config.CodeConfig}
 * in that it represents <b>current state</b> (positions may have changed) rather
 * than initial configuration.</p>
 *
 * <h2>Original vs Current</h2>
 * <p>The same CodeState type is used for two different purposes:</p>
 * <ul>
 *   <li><b>Original Code State:</b> Captured at configuration time (engine stores as ogCodeState)
 *       <ul>
 *         <li>Contains initial positions</li>
 *         <li>Used as key for history grouping</li>
 *         <li>Used as target for reset operations</li>
 *       </ul>
 *   </li>
 *   <li><b>Current Code State:</b> Captured at any time (via machine.getCodeState())
 *       <ul>
 *         <li>Contains current positions (after processing)</li>
 *         <li>Used for display and state queries</li>
 *         <li>Changes as characters are processed</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li><b>rotorIds:</b> Rotor identifiers in left→right order (e.g., [1, 2, 3])</li>
 *   <li><b>positions:</b> Current rotor positions as string (e.g., "ODX")
 *       <ul>
 *         <li>Left→right order</li>
 *         <li>Changes during processing</li>
 *         <li>Represents visible window characters</li>
 *       </ul>
 *   </li>
 *   <li><b>notchDist:</b> Distance to next notch for each rotor (e.g., [5, 12, 3])
 *       <ul>
 *         <li>Number of steps until rotor triggers stepping of next rotor</li>
 *         <li>Useful for predicting when double-stepping will occur</li>
 *       </ul>
 *   </li>
 *   <li><b>reflectorId:</b> Reflector identifier (e.g., "I", "II")</li>
 *   <li><b>plugboard:</b> Plugboard pairs (e.g., "ABCD" = A↔B, C↔D), "" = none</li>
 * </ul>
 *
 * <h2>String Format</h2>
 * <p>toString() produces detailed format with notch distances:</p>
 * <pre>{@code <1,2,3><O(5),D(12),X(3)><I>}</pre>
 * <ul>
 *   <li>First group: rotor IDs (left→right)</li>
 *   <li>Second group: positions with notch distances (e.g., O(5) = position O, 5 steps to notch)</li>
 *   <li>Third group: reflector ID</li>
 *   <li>Plugboard not shown (for brevity)</li>
 * </ul>
 *
 * <h2>Equality and Hashing</h2>
 * <p>As a record, CodeState uses structural equality. Two states are equal if
 * all fields are equal. This is important for history grouping:</p>
 * <ul>
 *   <li>Same rotors at same positions = same original code</li>
 *   <li>Same rotors at different positions = different original codes</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Get current state from machine
 * CodeState current = machine.getCodeState();
 * System.out.println("Current: " + current);  // <1,2,3><O(5),D(12),X(3)><I>
 *
 * // Store as original code in engine
 * CodeState original = machine.getCodeState();
 * history.recordConfig(original);
 *
 * // After processing, positions change but original remains
 * machine.process('A');
 * CodeState afterProcessing = machine.getCodeState();
 * // current positions differ, but original is unchanged
 * </pre>
 *
 * @param rotorIds rotor IDs in left→right order
 * @param positions current rotor positions as string (left→right)
 * @param notchDist distance to next notch for each rotor
 * @param reflectorId reflector identifier
 * @param plugboard plugboard pairs (empty string if none)
 * @since 1.0
 */
public record CodeState(
        List<Integer> rotorIds,
        String positions,
        List<Integer> notchDist,
        String reflectorId,
        String plugboard
) {

    /**
     * A canonical sentinel CodeState used to indicate "not configured".
     * <p>Use {@link #notConfigured()} or this constant when the machine has no active configuration.
     */
    public static final CodeState NOT_CONFIGURED = new CodeState(List.of(), "", List.of(), "", "");

    /**
     * Factory accessor for the canonical not-configured state.
     * @return canonical {@link CodeState} representing an unconfigured machine
     */
    public static CodeState notConfigured() {
        return NOT_CONFIGURED;
    }

    /**
     * Returns a detailed string representation including notch distances.
     *
     * <p>Format: {@code <rotorIds><positions(notchDist)><reflectorId>}</p>
     * <p>Example: {@code <1,2,3><O(5),D(12),X(3)><I>}</p>
     *
     * <p>The notch distance (number in parentheses) shows how many steps
     * until each rotor triggers stepping of the next rotor to its left.</p>
     *
     * @return detailed state string with notch distances
     */
    @Override
    public String toString() {

        // Format rotor IDs: <1,2,3>
        String ids = rotorIds.toString().replaceAll("[\\[\\] ]", "");

        // Format window positions with notch distances: <A(2),O(5),X(20)>
        StringBuilder posBuilder = new StringBuilder();
        for (int i = 0; i < positions.length(); i++) {
            char windowChar = positions.charAt(i);
            int dist = notchDist.get(i);

            posBuilder.append(windowChar)
                    .append("(")
                    .append(dist)
                    .append(")");

            if (i < positions.length() - 1) {
                posBuilder.append(",");
            }
        }

        // Reflector ID is expected to be a full Roman numeral string

        return "<%s><%s><%s>".formatted(ids, posBuilder, reflectorId);
    }
    /**
     * Convert this {@link CodeState} into an immutable {@link CodeConfig}
     * using the current rotor IDs, window positions and reflector ID.
     *
     * <p>Note:</p>
     * <ul>
     *   <li>Positions string (e.g. "ODX") is converted to a List of Characters
     *       (['O','D','X']) in left→right order.</li>
     *   <li>Plugboard is ignored for now because {@link CodeConfig} does not
     *       contain a plugboard field yet (Exercise 2).</li>
     * </ul>
     *
     * @return a CodeConfig built from this state
     * @throws IllegalStateException if this state represents NOT_CONFIGURED
     */
    public CodeConfig toCodeConfig() {
        // Guard against using the sentinel NOT_CONFIGURED as a real config
        if (this == NOT_CONFIGURED) {
            throw new IllegalStateException(
                    "Cannot convert NOT_CONFIGURED state to CodeConfig");
        }

        // Convert positions string (e.g. "ODX") to List<Character> ['O','D','X']
        List<Character> positionChars = new ArrayList<>(positions.length());
        for (int i = 0; i < positions.length(); i++) {
            positionChars.add(positions.charAt(i));
        }
        // rotorIds is already a List<Integer>, reflectorId is already a String
        return new CodeConfig(rotorIds, positionChars, reflectorId);
    }
}
