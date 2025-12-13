package enigma.engine;

import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EngineValidator — stateless validation helpers for engine configuration.
 * One-line: validate a CodeConfig against a MachineSpec.
 * Usage:
 * <pre>
 * EngineValidator.validateCodeConfig(spec, config);
 * </pre>
 *
 * Important invariants:
 * - Methods are static and side-effect free.
 * - Throw domain-specific exceptions (InvalidConfigurationException, InvalidMessageException) on validation failure.
 * - All error messages include: what is wrong, where (specific ID/index), and how to fix.
 */
public final class EngineValidator {

    private static final char ESC_CHAR = '\u001B'; // ESC character (ASCII 27)
    private EngineValidator() { /* utility */ }

    private static void specIsNull(MachineSpec spec) {
        if (spec == null) {
            throw new InvalidConfigurationException(
                    "Configuration validation failed: Machine specification is missing. " +
                            "Fix: Load a machine specification before configuring.");
        }
    }

    public static void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        // Validate spec
        specIsNull(spec);

        if (config == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: Configuration details are missing. " +
                "Fix: Provide complete configuration details.");
        }

        List<Integer> rotorIds = config.rotorIds();
        List<Character> positions = config.positions();
        String reflectorId = config.reflectorId();

        validateNullChecks(rotorIds, positions, reflectorId);
        validateRotorAndPositionCounts(spec, rotorIds, positions);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
        validateReflectorExists(spec, reflectorId);
        validatePositionsInAlphabet(spec, positions);
        // Plugboard validation is available for future use (exercise 2). Call with empty string (no-op)
        validatePlugboard(spec, "");
    }

    public static void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId) {
        if (rotorIds == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: Rotor IDs are missing. " +
                "Fix: Provide a list of rotor IDs (e.g., [1, 2, 3]).");
        }
        if (positions == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: Initial positions are missing. " +
                "Fix: Provide a list of initial positions (e.g., ['A', 'A', 'A']).");
        }
        if (reflectorId == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: Reflector ID is missing. " +
                "Fix: Provide a reflector ID (e.g., 'I', 'II', etc.).");
        }
    }

    public static void validateRotorAndPositionCounts(MachineSpec spec, List<Integer> rotorIds, List<Character> positions) {
        // Validate spec
        specIsNull(spec);

        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException("Expected exactly " + required + " rotors, got " + rotorIds.size());
        }
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                String.format(
                    "Position count mismatch: Expected exactly %d initial positions, but got %d." +
                    " Provided positions: %s. " +
                    "Fix: Provide exactly %d initial positions (one per rotor).",
                    required, positions.size(), positions, required));
        }
    }
    /**
     * Validates that the number of initial positions matches the number of rotors required by the machine specification.
     *
     * @param spec      the machine specification containing the required number of rotors
     * @param positions the list of initial positions to validate
     * @throws InvalidConfigurationException if the number of positions does not match the number of rotors in use
     * Usage:
     * <pre>
     * EngineValidator.validatePositionCounts(spec, positions);
     * </pre>
     */
    public static void validatePositionCounts(MachineSpec spec, List<Character> positions) {
        // Validate spec
        specIsNull(spec);
        int required = spec.getRotorsInUse();
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                    String.format(
                            "Position count mismatch: Expected exactly %d initial positions, but got %d. " +
                                    "Provided positions: %s. " +
                                    "Fix: Provide exactly %d initial positions (one per rotor).",
                            required, positions.size(), positions, required));
        }
    }
    /**
     * Validates that the number of selected rotors matches the required rotor count.
     *
     * <p>
     * This method checks that the provided list of rotor IDs matches the number of rotors
     * required by the machine specification. If the count does not match, an
     * {@link InvalidConfigurationException} is thrown with a detailed error message.
     * </p>
     *
     * @param spec      the machine specification (must not be {@code null})
     * @param rotorIds  the list of selected rotor IDs (must not be {@code null})
     * @throws InvalidConfigurationException if the number of selected rotors does not match the required count,
     *                                       or if {@code spec} is {@code null}
     */
    public static void validateRotorCount(MachineSpec spec, List<Integer> rotorIds) {
        // Validate spec
        specIsNull(spec);
        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException(
                    String.format(
                            "Invalid rotor selection: Exactly %d rotors must be selected, but got %d. " +
                                    "Provided rotor IDs: %s. " +
                                    "Fix: Select exactly %d rotor IDs from the available rotors in the machine specification.",
                            required, rotorIds.size(), rotorIds, required
                    )
            );
        }
    }



    public static void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> availableRotorIds = new HashSet<>(spec.rotorsById().keySet());

        for (int id : rotorIds) {
            // Check for duplicates
            if (!seen.add(id)) {
                throw new InvalidConfigurationException(
                        String.format(
                                "Duplicate rotor ID detected: Rotor %d appears more than once in the configuration", id));
            }

            // Check if rotor exists in spec
            if (spec.getRotorById(id) == null) {
                throw new InvalidConfigurationException("Invalid rotor ID: Rotor " + id + " does not exist in the machine specification (Available rotors " + availableRotorIds + ")");
            }
        }
    }

    public static void validateReflectorExists(MachineSpec spec, String reflectorId) {
        if (reflectorId.isBlank()) {
            throw new InvalidConfigurationException(
                "Invalid reflector ID: reflectorId must be non-empty. " +
                "Fix: Provide a valid reflector ID (e.g., 'I', 'II', 'III', etc.).");
        }

        if (spec.getReflectorById(reflectorId) == null) {
            Set<String> availableReflectorIds = spec.reflectorsById().keySet();
            throw new InvalidConfigurationException(
                String.format(
                    "Invalid reflector ID: Reflector '%s' does not exist in the machine specification. " +
                    "Available reflector IDs: %s. " +
                    "Fix: Choose from the available reflector IDs listed above.",
                    reflectorId, availableReflectorIds));
        }
    }

    public static void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions) {
        String alphabet = spec.alphabet().getLetters();

        for (char c : positions) {
            if (!spec.alphabet().contains(c)) {
                throw new InvalidConfigurationException(
                        String.format(
                                "Position must be in the Alphabet, '%c' is not a valid position",
                                c));
            }
        }
    }

    /**
     * Validate that input message contains only valid alphabet characters.
     *
     * <p>Validation rules:
     * <ul>
     *   <li>All characters in {@code input} must be present in the machine alphabet (obtained from {@code spec}).</li>
     *   <li>ISO control characters (newline, tab, ESC, etc.) are rejected because they are not printable and
     *       would interfere with processing and logging.</li>
     * </ul>
     *
     * <p>Failure behavior: this method throws {@link InvalidMessageException} with a clear, actionable message
     * describing the offending character (or control name), its index, a truncated view of the input, and a fix.
     *
     * @param spec machine specification containing the alphabet; must be non-null
     * @param input input message to validate; must be non-null
     * @throws InvalidMessageException if {@code spec} or {@code input} is null, if the input contains ISO control
     *                                 characters, or if it contains characters not present in the machine alphabet
     */
    public static void validateInputInAlphabet(MachineSpec spec, String input) {
        // Validate spec
        specIsNull(spec);

        if (input == null) {
            throw new InvalidMessageException(
                "Message validation failed: Input message is missing. " +
                "Fix: Provide a message to process.");
        }

        String alphabet = spec.alphabet().getLetters();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Reject control characters explicitly (newline, tab, escape, etc.)
            if (Character.isISOControl(c)) {
                String controlName = getControlCharacterName(c);
                throw new InvalidMessageException(
                    String.format(
                        "Invalid character in message: Control character %s detected at position %d. " +
                        "Input: \"%s\". " +
                        "Fix: Remove all control characters (newline, tab, ESC, etc.) from the message.",
                        controlName, i, truncateForDisplay(input, 50)));
            }

            if (!spec.alphabet().contains(c)) {
                throw new InvalidMessageException(
                    String.format(
                        "Invalid character in message: Character '%c' at position %d is not in the machine alphabet. " +
                        "Machine alphabet: %s. " +
                        "Input: \"%s\". " +
                        "Fix: Use only characters from the machine alphabet.",
                        c, i, alphabet, truncateForDisplay(input, 50)));
            }
        }
    }
    
    /**
     * Get a human-friendly name for a control character for use in error messages.
     *
     * <p>Maps common control code points to readable identifiers (for clarity):
     * <ul>
     *   <li>0  -> "NULL"</li>
     *   <li>9  -> "TAB"</li>
     *   <li>10 -> "NEWLINE (\\n)"</li>
     *   <li>13 -> "CARRIAGE RETURN (\\r)"</li>
     *   <li>27 -> "ESC"</li>
     * </ul>
     * Any other ISO control character returns the generic "CONTROL" label.
     *
     * @param c control character to describe
     * @return a short, human-readable name for the control character
     */
    private static String getControlCharacterName(char c) {
        return switch ((int) c) {
            case 0 -> "NULL";
            case 9 -> "TAB";
            case 10 -> "NEWLINE (\\n)";
            case 13 -> "CARRIAGE RETURN (\\r)";
            case 27 -> "ESC";
            default -> "CONTROL";
        };
    }
    
    /**
     * Truncate a string for display with ellipsis if it exceeds {@code maxLen} characters.
     *
     * <p>Before truncation, control and non-printable characters are escaped to visible sequences
     * using {@link #escapeControlChars(String)} so the returned representation is safe for logs
     * and error messages. If the escaped representation is longer than {@code maxLen}, it is cut
     * and suffixed with "..." to indicate truncation.
     *
     * @param str original string to prepare for display (may be null)
     * @param maxLen maximum length of the returned string before appending ellipsis
     * @return an escaped and possibly truncated representation suitable for error messages;
     *         returns {@code null} if {@code str} is {@code null}
     */
    private static String truncateForDisplay(String str, int maxLen) {
        if (str == null) {
            return null;
        }

        // First, escape control/non-printable characters to visible markers (e.g., \n, \t, ESC)
        String escaped = escapeControlChars(str);

        if (escaped.length() <= maxLen) {
            return escaped;
        }
        return escaped.substring(0, maxLen) + "...";
    }

    /**
     * Replace ISO control and other non-printable characters with visible escape sequences.
     *
     * <p>Examples:
     * <ul>
     *   <li>newline -> "\\n"</li>
     *   <li>tab -> "\\t"</li>
     *   <li>ESC -> "\\u001B"</li>
     *   <li>other control characters -> "\\uXXXX" (hex code)</li>
     * </ul>
     * The result is safe to include in logs and error messages where raw control characters
     * would otherwise be invisible or disruptive.</p>
     *
     * @param s input string to escape (assumed non-null by callers)
     * @return string where control/non-printable characters are replaced with readable escape sequences
     */
    private static String escapeControlChars(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch ((int) c) {
                case 0 -> sb.append("\\0");
                case 9 -> sb.append("\\t");
                case 10 -> sb.append("\\n");
                case 13 -> sb.append("\\r");
                case 27 -> sb.append("\\u001B");
                default -> {
                    if (Character.isISOControl(c)) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Validate plugboard configuration string.
     *
     * <p>Plugboard validation rules:</p>
     * <ul>
     *   <li>Must be even-length (pairs of characters)</li>
     *   <li>No character may appear more than once</li>
     *   <li>No character may be mapped to itself (e.g., "AA")</li>
     *   <li>All characters must exist in the machine alphabet</li>
     * </ul>
     *
     * <p>This method is prepared for Exercise 2 when plugboard is added to CodeConfig.
     * For now, it can be called with null or empty string to indicate no plugboard.</p>
     *
     * @param spec machine specification containing the alphabet
     * @param plugboard plugboard configuration string (e.g., "ABCD" maps A↔B and C↔D), may be null or empty
     */
    private static void validatePlugboard(MachineSpec spec, String plugboard) {
        // Validate spec
        specIsNull(spec);
        
        // null or empty plugboard is valid (no plugboard configured)
        if (plugboard == null || plugboard.isEmpty()) {
            return;
        }

        // Check even length
        if (plugboard.length() % 2 != 0) {
            throw new InvalidConfigurationException(
                String.format(
                    "Invalid plugboard configuration: Length must be even (pairs of characters), but got length %d. " +
                    "Plugboard: \"%s\". " +
                    "Fix: Provide pairs of characters (e.g., \"ABCD\" for A↔B and C↔D).",
                    plugboard.length(), plugboard));
        }

        Set<Character> seenChars = new HashSet<>();
        String alphabet = spec.alphabet().getLetters();

        // Process pairs
        for (int i = 0; i < plugboard.length(); i += 2) {
            char first = plugboard.charAt(i);
            char second = plugboard.charAt(i + 1);
            String pair = "" + first + second;

            // Check for self-mapping
            if (first == second) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid plugboard pair: Letter '%c' cannot be mapped to itself (pair \"%s\" at position %d). " +
                        "Plugboard: \"%s\". " +
                        "Fix: Each pair must consist of two different letters.",
                        first, pair, i, plugboard));
            }

            // Check for duplicate characters
            if (!seenChars.add(first)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Duplicate plugboard letter: Letter '%c' appears more than once (pair \"%s\" at position %d). " +
                        "Plugboard: \"%s\". " +
                        "Fix: Each letter can only appear once in the plugboard configuration.",
                        first, pair, i, plugboard));
            }
            if (!seenChars.add(second)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Duplicate plugboard letter: Letter '%c' appears more than once (pair \"%s\" at position %d). " +
                        "Plugboard: \"%s\". " +
                        "Fix: Each letter can only appear once in the plugboard configuration.",
                        second, pair, i, plugboard));
            }

            // Check characters are in alphabet
            if (!spec.alphabet().contains(first)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid plugboard character: Character '%c' (in pair \"%s\" at position %d) is not in the machine alphabet. " +
                        "Machine alphabet: %s. " +
                        "Plugboard: \"%s\". " +
                        "Fix: Use only characters from the machine alphabet.",
                        first, pair, i, alphabet, plugboard));
            }
            if (!spec.alphabet().contains(second)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid plugboard character: Character '%c' (in pair \"%s\" at position %d) is not in the machine alphabet. " +
                        "Machine alphabet: %s. " +
                        "Plugboard: \"%s\". " +
                        "Fix: Use only characters from the machine alphabet.",
                        second, pair, i, alphabet, plugboard));
            }
        }
    }
    /**
     * Validates the rotor configuration for the machine.
     * <p>
     * Checks that the number of rotors matches the machine's required count,
     * that all specified rotor IDs exist in the machine specification, and that
     * there are no duplicate rotor IDs.
     *
     * @param spec     the machine specification to validate against (must not be null)
     * @param rotorIds the list of rotor IDs to validate (must not be null)
     * @throws InvalidConfigurationException if the rotor configuration is invalid,
     *         including incorrect rotor count, non-existent rotor IDs, or duplicate IDs.
     * @see #validateRotorCount(MachineSpec, List)
     * @see #validateRotorIdsExistenceAndUniqueness(MachineSpec, List)
     */
    public static void validateRotors(MachineSpec spec, List<Integer> rotorIds) {
        validateRotorCount(spec, rotorIds);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
    }
    /**
     * Validates the initial positions for the rotors against the machine specification.
     * <p>
     * Checks that the number of positions matches the required rotor count and that
     * each position character is present in the machine's alphabet.
     *
     * @param spec      the machine specification to validate against (must not be null)
     * @param positions the list of initial rotor positions (must not be null)
     * @throws InvalidConfigurationException if the number of positions is incorrect or
     *         if any position character is not in the machine's alphabet
     * @see #validatePositionCounts(MachineSpec, List)
     * @see #validatePositionsInAlphabet(MachineSpec, List)
     */
    public static void validatePositions(MachineSpec spec, List<Character> positions) {
        validatePositionCounts(spec, positions);
        validatePositionsInAlphabet(spec, positions);
    }

}
