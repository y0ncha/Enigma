package enigma.engine;

import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stateless validation helpers for engine configuration.
 *
 * <p>Validates CodeConfig instances against MachineSpec requirements.
 * All methods are static and side-effect free.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * EngineValidator.validateCodeConfig(spec, config);
 * </pre>
 */
public final class EngineValidator {

    private static final char ESC_CHAR = '\u001B'; // ESC character (ASCII 27)
    private EngineValidator() { /* utility */ }

    private static void specIsNull(MachineSpec spec) {
        if (spec == null) {
            throw new InvalidConfigurationException("Machine specification is missing");
        }
    }

    public static void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        specIsNull(spec);

        if (config == null) {
            throw new InvalidConfigurationException("Configuration details are missing");
        }

        List<Integer> rotorIds = config.rotorIds();
        List<Character> positions = config.positions();
        String reflectorId = config.reflectorId();

        validateNullChecks(rotorIds, positions, reflectorId);
        validateRotorAndPositionCounts(spec, rotorIds, positions);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
        validateReflectorExists(spec, reflectorId);
        validatePositionsInAlphabet(spec, positions);
        validatePlugboard(spec, "");
    }

    public static void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId) {
        if (rotorIds == null) {
            throw new InvalidConfigurationException("Rotor IDs are missing");
        }
        if (positions == null) {
            throw new InvalidConfigurationException("Initial positions are missing");
        }
        if (reflectorId == null) {
            throw new InvalidConfigurationException("Reflector ID is missing");
        }
    }

    public static void validateRotorAndPositionCounts(MachineSpec spec, List<Integer> rotorIds, List<Character> positions) {
        specIsNull(spec);

        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException(
                String.format("Rotor count mismatch: expected %d, got %d", required, rotorIds.size()));
        }
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                String.format("Position count mismatch: expected %d, got %d", required, positions.size()));
        }
    }
    /**
     * Validates that the number of initial positions matches the number of rotors.
     *
     * @param spec      the machine specification containing the required number of rotors
     * @param positions the list of initial positions to validate
     * @throws InvalidConfigurationException if the number of positions does not match the number of rotors in use
     */
    public static void validatePositionCounts(MachineSpec spec, List<Character> positions) {
        specIsNull(spec);
        int required = spec.getRotorsInUse();
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                String.format("Position count mismatch: expected %d, got %d", required, positions.size()));
        }
    }
    /**
     * Validates that the number of selected rotors matches the required rotor count.
     *
     * @param spec      the machine specification (must not be null)
     * @param rotorIds  the list of selected rotor IDs (must not be null)
     * @throws InvalidConfigurationException if the number of selected rotors does not match the required count
     */
    public static void validateRotorCount(MachineSpec spec, List<Integer> rotorIds) {
        specIsNull(spec);
        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException(
                String.format("Rotor count mismatch: expected %d, got %d", required, rotorIds.size()));
        }
    }



    public static void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> availableRotorIds = new HashSet<>(spec.rotorsById().keySet());

        for (int id : rotorIds) {
            if (!seen.add(id)) {
                throw new InvalidConfigurationException(
                    String.format("Rotor %d appears more than once", id));
            }

            if (spec.getRotorById(id) == null) {
                throw new InvalidConfigurationException(
                    String.format("Rotor %d does not exist (available: %s)", id, availableRotorIds));
            }
        }
    }

    public static void validateReflectorExists(MachineSpec spec, String reflectorId) {
        if (reflectorId.isBlank()) {
            throw new InvalidConfigurationException("Reflector ID cannot be empty");
        }

        if (spec.getReflectorById(reflectorId) == null) {
            Set<String> availableReflectorIds = spec.reflectorsById().keySet();
            throw new InvalidConfigurationException(
                String.format("Reflector '%s' does not exist (available: %s)", 
                    reflectorId, availableReflectorIds));
        }
    }

    public static void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions) {
        String alphabet = spec.alphabet().getLetters();

        for (char c : positions) {
            if (!spec.alphabet().contains(c)) {
                throw new InvalidConfigurationException(
                    String.format("Position '%c' is not in the alphabet", c));
            }
        }
    }

    /**
     * Validate that input message contains only valid alphabet characters.
     *
     * <p>Rejects control characters (newline, tab, ESC, etc.) and characters
     * not present in the machine alphabet.</p>
     *
     * @param spec machine specification containing the alphabet
     * @param input input message to validate
     * @throws InvalidMessageException if input contains invalid characters
     */
    public static void validateInputInAlphabet(MachineSpec spec, String input) {
        specIsNull(spec);

        if (input == null) {
            throw new InvalidMessageException("Input message is missing");
        }

        String alphabet = spec.alphabet().getLetters();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isISOControl(c)) {
                String controlName = getControlCharacterName(c);
                throw new InvalidMessageException(
                    String.format("Control character %s at position %d", controlName, i));
            }

            if (!spec.alphabet().contains(c)) {
                throw new InvalidMessageException(
                    String.format("Character '%c' at position %d is not in the machine alphabet", c, i));
            }
        }
    }
    
    /**
     * Get a human-friendly name for a control character.
     *
     * @param c control character to describe
     * @return a short, human-readable name for the control character
     */
    private static String getControlCharacterName(char c) {
        return switch ((int) c) {
            case 0 -> "NULL";
            case 9 -> "TAB";
            case 10 -> "NEWLINE";
            case 13 -> "CARRIAGE RETURN";
            case 27 -> "ESC";
            default -> "CONTROL";
        };
    }
    
    /**
     * Truncate a string for display with ellipsis if it exceeds the maximum length.
     *
     * <p>Control characters are escaped to visible sequences before truncation.</p>
     *
     * @param str original string to prepare for display
     * @param maxLen maximum length before appending ellipsis
     * @return escaped and possibly truncated representation
     */
    private static String truncateForDisplay(String str, int maxLen) {
        if (str == null) {
            return null;
        }

        String escaped = escapeControlChars(str);

        if (escaped.length() <= maxLen) {
            return escaped;
        }
        return escaped.substring(0, maxLen) + "...";
    }

    /**
     * Replace control characters with visible escape sequences.
     *
     * @param s input string to escape
     * @return string where control characters are replaced with escape sequences
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
     * <p>Requirements: even length, no duplicates, no self-mapping, 
     * all characters in alphabet. Null or empty indicates no plugboard.</p>
     *
     * @param spec machine specification containing the alphabet
     * @param plugboard plugboard configuration string (e.g., "ABCD" maps A↔B and C↔D)
     */
    private static void validatePlugboard(MachineSpec spec, String plugboard) {
        specIsNull(spec);
        
        if (plugboard == null || plugboard.isEmpty()) {
            return;
        }

        if (plugboard.length() % 2 != 0) {
            throw new InvalidConfigurationException(
                String.format("Plugboard length must be even, got %d", plugboard.length()));
        }

        Set<Character> seenChars = new HashSet<>();
        String alphabet = spec.alphabet().getLetters();

        for (int i = 0; i < plugboard.length(); i += 2) {
            char first = plugboard.charAt(i);
            char second = plugboard.charAt(i + 1);

            if (first == second) {
                throw new InvalidConfigurationException(
                    String.format("Plugboard letter '%c' cannot map to itself", first));
            }

            if (!seenChars.add(first)) {
                throw new InvalidConfigurationException(
                    String.format("Plugboard letter '%c' appears more than once", first));
            }
            if (!seenChars.add(second)) {
                throw new InvalidConfigurationException(
                    String.format("Plugboard letter '%c' appears more than once", second));
            }

            if (!spec.alphabet().contains(first)) {
                throw new InvalidConfigurationException(
                    String.format("Plugboard character '%c' is not in the machine alphabet", first));
            }
            if (!spec.alphabet().contains(second)) {
                throw new InvalidConfigurationException(
                    String.format("Plugboard character '%c' is not in the machine alphabet", second));
            }
        }
    }
    /**
     * Validates the rotor configuration.
     *
     * <p>Checks count, existence, and uniqueness of rotor IDs.</p>
     *
     * @param spec     the machine specification to validate against
     * @param rotorIds the list of rotor IDs to validate
     * @throws InvalidConfigurationException if the rotor configuration is invalid
     */
    public static void validateRotors(MachineSpec spec, List<Integer> rotorIds) {
        validateRotorCount(spec, rotorIds);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
    }
    /**
     * Validates the initial positions for the rotors.
     *
     * <p>Checks count and alphabet membership.</p>
     *
     * @param spec      the machine specification to validate against
     * @param positions the list of initial rotor positions
     * @throws InvalidConfigurationException if positions are invalid
     */
    public static void validatePositions(MachineSpec spec, List<Character> positions) {
        validatePositionCounts(spec, positions);
        validatePositionsInAlphabet(spec, positions);
    }

}
