package enigma.shared.state;

import enigma.shared.dto.config.CodeConfig;

import java.util.ArrayList;
import java.util.List;

import static enigma.shared.utils.Utils.formatPlugboard;

/**
 * Snapshot of code configuration and current rotor positions.
 *
 * <p>Captures rotor IDs, positions, notch distances, reflector ID, and plugStr.
 * Used for both original configuration (at setup) and current state (after processing).</p>
 *
 * @param rotorIds rotor IDs in left→right order
 * @param positions current rotor positions as string
 * @param notchDist distance to next notch for each rotor
 * @param reflectorId reflector identifier
 * @param plugStr plugStr pairs
 * @since 1.0
 */
public record CodeState(
        List<Integer> rotorIds,
        String positions,
        List<Integer> notchDist,
        String reflectorId,
        String plugStr
) {

    /**
     * Sentinel value representing unconfigured state.
     */
    public static final CodeState NOT_CONFIGURED = new CodeState(List.of(), "", List.of(), "", "");

    /**
     * Return sentinel not-configured state.
     *
     * @return not-configured state
     */
    public static CodeState notConfigured() {
        return NOT_CONFIGURED;
    }

    /**
     * Return string representation with notch distances.
     *
     * @return state string in format {@code <rotorIds><positions(notchDist)><reflectorId>}
     */
    @Override
    public String toString() {

        // Format rotors with positions and notch distances
        String ids = "<" + rotorIds.toString().replaceAll("[\\[\\] ]", "") + ">";
        StringBuilder rotorStr = new StringBuilder();
        rotorStr.append("<");
        for (int i = 0; i < positions.length(); i++) {
            char windowChar = positions.charAt(i);
            int dist = notchDist.get(i);

            rotorStr.append(windowChar)
                    .append("(")
                    .append(dist)
                    .append(")");

            if (i < positions.length() - 1) {
                rotorStr.append(",");
            }
        }
        rotorStr.append(">");

        // Format reflector id
        String reflectorStr = "<" + reflectorId + ">";

        // If plugStr is configured, format it
        String plugboardStr = "";
        if (plugStr != null && !plugStr.isEmpty()) {
            plugboardStr = "<" + formatPlugboard(plugStr) + ">";
        }
        return ids + rotorStr + reflectorStr + plugboardStr;
    }

    /**
     * Convert state to code configuration.
     *
     * @return code configuration from this state
     * @throws IllegalStateException if state is NOT_CONFIGURED
     */
    public CodeConfig toConfig() {
        if (this == NOT_CONFIGURED) {
            throw new IllegalStateException(
                    "Code not configured");
        }

        List<Character> positionChars = new ArrayList<>(positions.length());
        for (int i = 0; i < positions.length(); i++) {
            positionChars.add(positions.charAt(i));
        }
        return new CodeConfig(rotorIds, positionChars, reflectorId, plugStr);
    }

}
