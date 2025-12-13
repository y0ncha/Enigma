package enigma.console.helper;

import enigma.console.ConsoleCommand;

/**
 * Console-level format validation helpers.
 *
 * <p><b>Module:</b> enigma-console (validation helpers)</p>
 *
 * <h2>Purpose</h2>
 * <p>ConsoleValidator provides stateless format-level validation methods for
 * console input. These methods check input format and structure without
 * performing semantic validation against the machine specification.</p>
 *
 * <h2>Validation Philosophy</h2>
 * <p><b>Format Only:</b> Console validator checks:</p>
 * <ul>
 *   <li>Input is non-empty</li>
 *   <li>Input can be parsed as the expected type (integer, etc.)</li>
 *   <li>Values are in the expected range (command IDs 1-8)</li>
 *   <li>Lengths match expectations (positions length = rotor count)</li>
 *   <li>Characters are A-Z (format check, not alphabet membership)</li>
 * </ul>
 *
 * <p><b>NOT Semantic:</b> Console validator does NOT check:</p>
 * <ul>
 *   <li>Rotor IDs exist in machine spec (engine responsibility)</li>
 *   <li>Rotor IDs are unique (engine responsibility)</li>
 *   <li>Reflector ID exists in machine spec (engine responsibility)</li>
 *   <li>Position characters are in the alphabet (engine responsibility)</li>
 *   <li>Plugboard has no duplicates (engine responsibility)</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>All methods throw {@link IllegalArgumentException} with user-friendly
 * error messages on validation failure. The console catches these exceptions
 * and either retries (format errors) or returns to a menu (semantic errors from engine).</p>
 *
 * <h2>Usage Pattern</h2>
 * <pre>
 * // Console reads input
 * String input = scanner.nextLine();
 *
 * try {
 *     // Format validation (console)
 *     ConsoleValidator.ensurePositionsLengthMatches(input, 3);
 *     // If valid, pass to engine for semantic validation
 *     engine.configManual(config);
 * } catch (IllegalArgumentException e) {
 *     // Format error - retry input loop
 *     System.err.println(e.getMessage());
 * } catch (InvalidConfigurationException e) {
 *     // Semantic error - return to menu
 *     System.err.println(e.getMessage());
 * }
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods are static and stateless. Thread-safe.</p>
 *
 * @since 1.0
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