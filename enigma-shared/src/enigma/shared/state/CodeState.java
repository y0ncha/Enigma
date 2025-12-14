package enigma.shared.state;

import enigma.shared.dto.config.CodeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of code configuration and current rotor positions.
 *
 * <p>Captures rotor IDs, positions, notch distances, reflector ID, and plugboard.
 * Used for both original configuration (at setup) and current state (after processing).</p>
 *
 * @param rotorIds rotor IDs in left→right order
 * @param positions current rotor positions as string
 * @param notchDist distance to next notch for each rotor
 * @param reflectorId reflector identifier
 * @param plugboard plugboard pairs
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

        String ids = rotorIds.toString().replaceAll("[\\[\\] ]", "");

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

        return "<%s><%s><%s>".formatted(ids, posBuilder, reflectorId);
    }

    /**
     * Convert state to code configuration.
     *
     * @return code configuration from this state
     * @throws IllegalStateException if state is NOT_CONFIGURED
     */
    public CodeConfig toCodeConfig() {
        if (this == NOT_CONFIGURED) {
            throw new IllegalStateException(
                    "Cannot convert NOT_CONFIGURED state to CodeConfig");
        }

        List<Character> positionChars = new ArrayList<>(positions.length());
        for (int i = 0; i < positions.length(); i++) {
            positionChars.add(positions.charAt(i));
        }
        return new CodeConfig(rotorIds, positionChars, reflectorId);
    }
}
