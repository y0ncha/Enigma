package enigma.console.helper;

import enigma.console.ConsoleCommand;

/**
 * Console-level format validation helpers.
 *
 * <p>Validates input format and structure without semantic checks.
 * Format-only: parsing, ranges, lengths. Does not validate against machine specification.</p>
 */
public final class ConsoleValidator {

    private ConsoleValidator() { /* utility class */ }

    /**
     * Parse and validate a command ID from raw user input.
     *
     * <p><b>Validation:</b></p>
     * <ul>
     *   <li>Input is non-empty</li>
     *   <li>Input can be parsed as integer</li>
     *   <li>Integer maps to a known command</li>
     * </ul>
     *
     * @param raw raw user input (maybe null, empty, or invalid)
     * @return validated ConsoleCommand
     * @throws IllegalArgumentException if input is invalid with a user-friendly message
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
            throw new IllegalArgumentException("Unknown command number, ");
        }
        ConsoleCommand cmd = ConsoleCommand.fromId(commandId);
        if (cmd == null) {
            throw new IllegalArgumentException("Unknown command number, ");
        }
        return cmd;
    }

    /**
     * Validate that reflector choice is in a valid range.
     *
     * <p><b>Validation:</b></p>
     * <ul>
     *   <li>The choice is between 1 and available reflector count (1-based)</li>
     * </ul>
     *
     * <p>This is a format check only. Engine validates that the chosen
     * reflector ID actually exists in the machine specification.</p>
     *
     * @param choice user's reflector choice (1-based)
     * @param reflectorsCount number of available reflectors
     * @throws IllegalArgumentException if the choice is out of range with a user-friendly message
     */
    public static void ensureReflectorChoiceInRange(int choice, int reflectorsCount) {
        if (choice < 1 || choice > reflectorsCount) {
            throw new IllegalArgumentException("Reflector choice must be between 1 and " + reflectorsCount + ".");
        }
    }

    /**
     * Validate plugboard format (even length).
     *
     * <p><b>Validation:</b></p>
     * <ul>
     *   <li>Null or empty is valid (no plugboard connections)</li>
     *   <li>If provided, must have even length (pairs of characters)</li>
     * </ul>
     *
     * <p><b>Format Only:</b> This method does NOT check:</p>
     * <ul>
     *   <li>Duplicate characters (engine validates)</li>
     *   <li>Self-mapping (engine validates)</li>
     *   <li>Alphabet membership (engine validates)</li>
     * </ul>
     *
     * @param plugboard plugboard string (e.g., "ABCD" for A ↔ B, C ↔ D)
     * @throws IllegalArgumentException if a format is invalid with a user-friendly message
     */
    public static void validatePlugboardFormat(String plugboard) {
        // null or empty is valid (no plugboard)
        if (plugboard == null || plugboard.isEmpty()) {
            return;
        }
        // Check even length (format requirement)
        if (plugboard.length() % 2 != 0) {
            throw new IllegalArgumentException("Plugboard must have even length (pairs of characters). Got " + plugboard.length());
        }
    }
}