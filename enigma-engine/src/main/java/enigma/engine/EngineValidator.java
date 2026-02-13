package enigma.engine;

import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates engine configurations and input messages against machine specifications.
 *
 * <p>Provides static validation methods for code configurations and message input.
 * All methods are side-effect free and throw domain-specific exceptions when
 * validation rules are violated.</p>
 *
 * @since 1.0
 */
public final class EngineValidator {

    /**
     * Prevent instantiation of utility class.
     */
    private EngineValidator() {}

    /**
     * Check if machine specification is null and throw exception.
     *
     * @param spec the machine specification to check
     * @throws InvalidConfigurationException if spec is null
     */
    private static void specIsNull(MachineSpec spec) {
        if (spec == null) {
            throw new InvalidConfigurationException(
                    "Machine specification is missing");
        }
    }

    /**
     * Validate complete code configuration against machine specification.
     *
     * <p>Performs comprehensive validation of a code configuration including:</p>
     * <ul>
     *   <li>Non-null configuration elements</li>
     *   <li>Rotor count matches specification requirement</li>
     *   <li>Position count matches rotor count</li>
     *   <li>All rotor IDs exist and are unique</li>
     *   <li>Reflector ID exists</li>
     *   <li>All position characters are in the alphabet</li>
     *   <li>Plugboard configuration is valid</li>
     * </ul>
     *
     * @param spec the machine specification to validate against
     * @param config the code configuration to validate
     * @throws InvalidConfigurationException if any validation rule is violated
     * @since 1.0
     */
    public static void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        // Validate spec
        specIsNull(spec);

        if (config == null) {
            throw new InvalidConfigurationException(
                "Configuration details are missing");
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
        validatePlugboard(spec, config.plugStr());
    }

    /**
     * Validate that configuration elements are non-null.
     *
     * <p>Checks that rotor IDs, positions, and reflector ID are all provided.</p>
     *
     * @param rotorIds list of rotor IDs
     * @param positions list of initial positions
     * @param reflectorId reflector identifier
     * @throws InvalidConfigurationException if any element is null
     * @since 1.0
     */
    public static void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId) {
        if (rotorIds == null) {
            throw new InvalidConfigurationException(
                "Rotor IDs are missing");
        }
        if (positions == null) {
            throw new InvalidConfigurationException(
                "Initial positions are missing");
        }
        if (reflectorId == null) {
            throw new InvalidConfigurationException(
                "Reflector ID is missing");
        }
    }

    /**
     * Validate rotor and position counts match specification requirement.
     *
     * <p>Ensures that the number of rotors and positions both equal the
     * required rotor count specified in the machine specification.</p>
     *
     * @param spec the machine specification
     * @param rotorIds list of rotor IDs
     * @param positions list of initial positions
     * @throws InvalidConfigurationException if counts do not match requirement
     * @since 1.0
     */
    public static void validateRotorAndPositionCounts(MachineSpec spec, List<Integer> rotorIds, List<Character> positions) {
        // Validate spec
        specIsNull(spec);

        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException("Expected exactly " + required + " rotors, but got " + rotorIds.size());
        }
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                String.format(
                    "Position count mismatch: Expected exactly %d initial positions, but got %d. \nProvided positions: %s. ",
                    required, positions.size(), positions));
        }
    }
    /**
     * Validate initial position count matches required rotor count.
     *
     * @param spec the machine specification
     * @param positions list of initial positions
     * @throws InvalidConfigurationException if position count is incorrect
     * @since 1.0
     */
    public static void validatePositionCounts(MachineSpec spec, List<Character> positions) {
        specIsNull(spec);
        int required = spec.getRotorsInUse();
        if (positions.size() != required) {
            throw new InvalidConfigurationException(
                    String.format(
                            "Expected exactly %d initial positions, got %d. " +
                                    "Provided positions: %s",
                            required, positions.size(), positions));
        }
    }

    /**
     * Validate rotor count matches required count.
     *
     * @param spec the machine specification
     * @param rotorIds list of rotor IDs
     * @throws InvalidConfigurationException if rotor count is incorrect
     * @since 1.0
     */
    public static void validateRotorCount(MachineSpec spec, List<Integer> rotorIds) {
        specIsNull(spec);
        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required) {
            throw new InvalidConfigurationException(
                    String.format(
                            "Exactly %d rotors must be selected, got %d. " +
                                    "Provided rotor IDs: %s",
                            required, rotorIds.size(), rotorIds
                    )
            );
        }
    }



    /**
     * Validate rotor IDs exist in specification and are unique.
     *
     * <p>Ensures that:</p>
     * <ul>
     *   <li>All rotor IDs exist in the machine specification</li>
     *   <li>No rotor ID appears more than once in the configuration</li>
     * </ul>
     *
     * @param spec the machine specification
     * @param rotorIds list of rotor IDs to validate
     * @throws InvalidConfigurationException if validation fails
     * @since 1.0
     */
    public static void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        Set<Integer> seen = new HashSet<>();
        Set<Integer> availableRotorIds = new HashSet<>(spec.rotorsById().keySet());

        for (int id : rotorIds) {
            if (!seen.add(id)) {
                throw new InvalidConfigurationException(
                        String.format(
                                "Rotor %d appears more than once in the configuration", id));
            }

            if (spec.getRotorById(id) == null) {
                throw new InvalidConfigurationException("Rotor " + id + " does not exist in the machine specification (available rotors: " + availableRotorIds + ")");
            }
        }
    }

    /**
     * Validate reflector ID exists in the machine specification.
     *
     * <p>Checks that the reflector ID is non-empty and exists in the
     * available reflectors defined in the machine specification.</p>
     *
     * @param spec the machine specification
     * @param reflectorId the reflector identifier to validate
     * @throws InvalidConfigurationException if reflector does not exist or ID is blank
     * @since 1.0
     */
    public static void validateReflectorExists(MachineSpec spec, String reflectorId) {
        if (reflectorId.isBlank()) {
            throw new InvalidConfigurationException(
                "Reflector ID must be non-empty");
        }

        if (spec.getReflectorById(reflectorId) == null) {
            Set<String> availableReflectorIds = spec.reflectorsById().keySet();
            throw new InvalidConfigurationException(
                String.format(
                    "Reflector '%s' does not exist in the machine specification. " +
                    "Available reflector IDs: %s",
                    reflectorId, availableReflectorIds));
        }
    }

    /**
     * Validate that position characters are in the machine alphabet.
     *
     * @param spec machine specification containing the alphabet
     * @param positions initial position characters to validate
     * @throws InvalidConfigurationException if any position is not in the alphabet
     * @since 1.0
     */
    public static void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions) {

        for (char c : positions) {
            if (!spec.alphabet().contains(c)) {
                throw new InvalidConfigurationException(
                        String.format(
                                "Position '%c' is not in the alphabet",
                                c));
            }
        }
    }

    /**
     * Validate input message contains only valid alphabet characters.
     *
     * <p>Rejects control characters (newline, tab, ESC) and characters
     * not present in the machine alphabet.</p>
     *
     * @param spec machine specification containing the alphabet
     * @param input input message to validate
     * @throws InvalidMessageException if input contains invalid characters
     * @since 1.0
     */
    public static void validateInputInAlphabet(MachineSpec spec, String input) {
        // Validate spec
        specIsNull(spec);

        if (input == null) {
            throw new InvalidMessageException("Input message is missing");
        }

        String alphabet = spec.alphabet().letters();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isISOControl(c)) {
                String controlName = getControlCharacterName(c);
                throw new InvalidMessageException(
                    String.format("Control character %s detected at position %d", controlName, i));
            }

            if (!spec.alphabet().contains(c)) {
                throw new InvalidMessageException(
                    String.format(
                        "Character '%c' at position %d is not in the machine alphabet : %s",
                        c, i, alphabet));
            }
        }
    }
    
    /**
     * Return human-readable name for control characters.
     *
     * @param c control character to describe
     * @return descriptive name for the control character
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
     * Replace control characters with visible escape sequences.
     *
     * @param s input string to escape
     * @return string with control characters escaped
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
     * Validate plugStr configuration string.
     *
     * <p>Validates even length, no duplicates, no self-mapping, and
     * all characters in alphabet.</p>
     *
     * @param spec machine specification containing the alphabet
     * @param plugboard plugStr configuration string, may be null or empty
     */
    private static void validatePlugboard(MachineSpec spec, String plugboard) {
        // Validate spec
        specIsNull(spec);
        
        // null or empty plugStr is valid (no plugStr configured)
        if (plugboard == null || plugboard.isEmpty()) {
            return;
        }

        if (plugboard.length() % 2 != 0) {
            throw new InvalidConfigurationException(
                String.format(
                    "Plugboard length must be even (pairs of characters), got length %d. " +
                    "Plugboard: \"%s\"",
                    plugboard.length(), plugboard));
        }

        Set<Character> seenChars = new HashSet<>();
        String alphabet = spec.alphabet().letters();

        for (int i = 0; i < plugboard.length(); i += 2) {
            char first = plugboard.charAt(i);
            char second = plugboard.charAt(i + 1);
            String pair = "" + first + second;

            if (first == second) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Plugboard letter '%c' cannot map to itself (pair \"%s\" at position %d). " +
                        "Plugboard: \"%s\"",
                        first, pair, i, plugboard));
            }

            if (!seenChars.add(first)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Plugboard letter '%c' appears more than once (pair \"%s\" at position %d). " +
                        "Plugboard: \"%s\"",
                        first, pair, i, plugboard));
            }
            if (!seenChars.add(second)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Plugboard letter '%c' appears more than once (pair \"%s\" at position %d). " +
                        "Plugboard: \"%s\"",
                        second, pair, i, plugboard));
            }

            if (!spec.alphabet().contains(first)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Plugboard character '%c' (in pair \"%s\" at position %d) is not in the machine alphabet. " +
                        "Machine alphabet: %s. " +
                        "Plugboard: \"%s\"",
                        first, pair, i, alphabet, plugboard));
            }
            if (!spec.alphabet().contains(second)) {
                throw new InvalidConfigurationException(
                    String.format(
                        "Plugboard character '%c' (in pair \"%s\" at position %d) is not in the machine alphabet. " +
                        "Machine alphabet: %s. " +
                        "Plugboard: \"%s\"",
                        second, pair, i, alphabet, plugboard));
            }
        }
    }
    /**
     * Validate rotor configuration: count, existence, and uniqueness.
     *
     * @param spec the machine specification
     * @param rotorIds list of rotor IDs to validate
     * @throws InvalidConfigurationException if rotor configuration is invalid
     * @since 1.0
     */
    public static void validateRotors(MachineSpec spec, List<Integer> rotorIds) {
        validateRotorCount(spec, rotorIds);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
    }

    /**
     * Validate initial positions: count and alphabet membership.
     *
     * @param spec the machine specification
     * @param positions list of initial rotor positions
     * @throws InvalidConfigurationException if positions are invalid
     * @since 1.0
     */
    public static void validatePositions(MachineSpec spec, List<Character> positions) {
        validatePositionCounts(spec, positions);
        validatePositionsInAlphabet(spec, positions);
    }


    /**
     * Validates the plugboard configuration string.
     * <p>
     * The plug string represents a sequence of plug pairs, where every two
     * consecutive characters define a single plug mapping.
     * </p>
     * <ul>
     *   <li>The string length must be even.</li>
     *   <li>All characters must belong to the machine alphabet.</li>
     *   <li>No character may be mapped to itself.</li>
     *   <li>No character may appear in more than one plug pair.</li>
     *   <li>An empty string is considered valid and represents no plugs.</li>
     * </ul>
     *
     * @param plugs    the continuous string representing plug pairs
     * @param alphabet the machine alphabet string
     * @throws IllegalArgumentException if the plug configuration is invalid
     * @since 2.0
     */
    public static void validatePlugs(String plugs, String alphabet) {
        if (plugs == null || plugs.isEmpty()) {
            return;
        }

        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("Machine alphabet is not defined.");
        }

        if (plugs.length() % 2 != 0) {
            throw new IllegalArgumentException(
                    "Plug string length must be even."
            );
        }

        Set<Character> usedChars = new HashSet<>();

        for (int i = 0; i < plugs.length(); i += 2) {
            char a = plugs.charAt(i);
            char b = plugs.charAt(i + 1);

            if (alphabet.indexOf(a) == -1 || alphabet.indexOf(b) == -1) {
                throw new IllegalArgumentException(
                        "Plug characters must be part of the machine alphabet [" + alphabet + "]"
                );
            }

            if (a == b) {
                throw new IllegalArgumentException(
                        "A character cannot be plugged to itself: " + a
                );
            }

            if (usedChars.contains(a) || usedChars.contains(b)) {
                throw new IllegalArgumentException(
                        "A character appears in more than one plug: " + a + " or " + b
                );
            }

            usedChars.add(a);
            usedChars.add(b);
        }
    }
}
