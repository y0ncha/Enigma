package enigma.console.helper;

import enigma.console.ConsoleCommand;

/**
 * Console-side, format-level validation helpers.
 * <p>
 * These methods perform only UI/input-format checks (empty, numeric, ranges,
 * lengths). They throw IllegalArgumentException with the exact user-facing
 * messages that the console prints so that the existing control-flow and
 * retry behavior can be preserved.
 */
public final class ConsoleValidator {

    private ConsoleValidator() { /* utility class */ }

    /**
     * Parse a raw user line into a ConsoleCommand.
     * Validates: non-empty, numeric, maps to a known command id.
     * Throws IllegalArgumentException containing the exact message the console
     * used previously so callers can print it unchanged.
     */
    public static ConsoleCommand parseCommand(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("No input provided. Please enter a number.");
        }
        final String trimmed = raw.trim();
        int commandId;
        try {
            commandId = Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input. Please enter a number between 1 and 8.");
        }
        ConsoleCommand cmd = ConsoleCommand.fromId(commandId);
        if (cmd == null) {
            throw new IllegalArgumentException("Unknown command number. Please choose a number between 1 and 8.");
        }
        return cmd;
    }

    /**
     * Ensure that the provided positions string length matches expected rotor count.
     * Throws IllegalArgumentException with the exact message the console used
     * previously when there's a mismatch.
     */
    public static void ensurePositionsLengthMatches(String positions, int rotorCount) {
        if (positions == null || positions.isEmpty()) {
            throw new IllegalArgumentException("Positions string cannot be empty.");
        }
        int len = positions.length();
        if (len != rotorCount) {
            throw new IllegalArgumentException("Number of positions (" + len
                    + ") must match number of rotors (" + rotorCount + ").");
        }
    }

    /**
     * Ensure that the chosen reflector number is within the displayed range.
     * Throws IllegalArgumentException with the same message the console used
     * previously when the choice was out of range.
     */
    public static void ensureReflectorChoiceInRange(int choice, int reflectorsCount) {
        if (choice < 1 || choice > reflectorsCount) {
            throw new IllegalArgumentException("Reflector choice must be between 1 and " + reflectorsCount + ".");
        }
    }

    /**
     * Validate basic format of plugboard input string.
     * Checks: non-null (null is valid), even length.
     * Does NOT check alphabet membership or semantic rules (engine handles that).
     * Throws IllegalArgumentException with user-facing message if format is invalid.
     */
    public static void validatePlugboardFormat(String plugboard) {
        // null or empty is valid (no plugboard)
        if (plugboard == null || plugboard.isEmpty()) {
            return;
        }
        // Check even length (format requirement)
        if (plugboard.length() % 2 != 0) {
            throw new IllegalArgumentException("Plugboard must have even length (pairs of characters). Got length: " + plugboard.length());
        }
    }
}