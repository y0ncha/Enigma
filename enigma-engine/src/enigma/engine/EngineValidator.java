package enigma.engine;

import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        if (spec == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: MachineSpec must not be null. " +
                "Fix: Load a machine specification before configuring.");
        }
        if (config == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: CodeConfig must not be null. " +
                "Fix: Provide a valid CodeConfig object.");
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
                "Configuration validation failed: rotorIds must not be null. " +
                "Fix: Provide a list of rotor IDs (e.g., [1, 2, 3]).");
        }
        if (positions == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: positions must not be null. " +
                "Fix: Provide a list of initial positions (e.g., ['A', 'A', 'A']).");
        }
        if (reflectorId == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: reflectorId must not be null. " +
                "Fix: Provide a reflector ID (e.g., 'I', 'II', etc.).");
        }
    }

    public static void validateRotorAndPositionCounts(MachineSpec spec, List<Integer> rotorIds, List<Character> positions) {
        if (spec == null) {
            throw new InvalidConfigurationException(
                "Configuration validation failed: MachineSpec must not be null. " +
                "Fix: Load a machine specification before configuring.");
        }
        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException(
                String.format(
                    "Rotor count mismatch: Expected exactly %d rotors, but got %d. " +
                    "Provided rotor IDs: %s. " +
                    "Fix: Select exactly %d rotor IDs from the available rotors in the machine specification.",
                    required, rotorIds.size(), rotorIds, required));
        }
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                String.format(
                    "Position count mismatch: Expected exactly %d initial positions, but got %d. " +
                    "Provided positions: %s. " +
                    "Fix: Provide exactly %d initial positions (one per rotor).",
                    required, positions.size(), positions, required));
        }
    }

    public static void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> availableRotorIds = spec.rotorsById().keySet();
        
        for (int i = 0; i < rotorIds.size(); i++) {
            int id = rotorIds.get(i);
            
            // Check for duplicates
            if (!seen.add(id)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Duplicate rotor ID detected: Rotor %d appears more than once in the configuration. " +
                        "Position in list: %d. " +
                        "All rotor IDs: %s. " +
                        "Fix: Each rotor can only be used once. Remove the duplicate rotor ID.",
                        id, i, rotorIds));
            }
            
            // Check if rotor exists in spec
            if (spec.getRotorById(id) == null) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid rotor ID: Rotor %d does not exist in the machine specification. " +
                        "Position in list: %d. " +
                        "Available rotor IDs: %s. " +
                        "Fix: Choose from the available rotor IDs listed above.",
                        id, i, availableRotorIds.stream().sorted().collect(Collectors.toList())));
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
        
        for (int i = 0; i < positions.size(); i++) {
            char c = positions.get(i);
            if (!spec.alphabet().contains(c)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Invalid position character: Position at index %d has invalid character '%c'. " +
                        "Machine alphabet: %s. " +
                        "All positions: %s. " +
                        "Fix: Use only characters from the machine alphabet.",
                        i, c, alphabet, positions));
            }
        }
    }

    /**
     * Validate that input message contains only valid alphabet characters.
     * 
     * <p>This validation ensures:</p>
     * <ul>
     *   <li>All characters must be in the machine alphabet</li>
     *   <li>No forbidden characters: newline (\n), tab (\t), ESC (ASCII 27), or other non-printables</li>
     * </ul>
     * 
     * @param spec machine specification containing the alphabet
     * @param input input message to validate
     * @throws InvalidMessageException if input contains invalid or forbidden characters
     */
    public static void validateInputInAlphabet(MachineSpec spec, String input) {
        if (spec == null) {
            throw new InvalidMessageException(
                "Message validation failed: MachineSpec must not be null. " +
                "Fix: Load a machine specification before processing messages.");
        }
        if (input == null) {
            throw new InvalidMessageException(
                "Message validation failed: Input must not be null. " +
                "Fix: Provide a non-null input string.");
        }
        
        String alphabet = spec.alphabet().getLetters();
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            // Reject control characters explicitly (newline, tab, escape, etc.)
            if (Character.isISOControl(c)) {
                String controlName = getControlCharacterName(c);
                throw new InvalidMessageException(
                    String.format(
                        "Invalid character in message: Control character %s detected at position %d (codepoint: %d). " +
                        "Input: \"%s\". " +
                        "Fix: Remove all control characters (newline, tab, ESC, etc.) from the message.",
                        controlName, i, (int)c, truncateForDisplay(input, 50)));
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
     * Get a human-friendly name for a control character.
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
     * Truncate a string for display with ellipsis if too long.
     */
    private static String truncateForDisplay(String str, int maxLen) {
        if (str.length() <= maxLen) {
            return str;
        }
        return str.substring(0, maxLen) + "...";
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
     * @throws InvalidConfigurationException if validation fails
     */
    private static void validatePlugboard(MachineSpec spec, String plugboard) {
        if (spec == null) {
            throw new InvalidConfigurationException(
                "Plugboard validation failed: MachineSpec must not be null. " +
                "Fix: Load a machine specification before configuring.");
        }
        
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
}
