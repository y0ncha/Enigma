package enigma.engine;

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
 * - Throw IllegalArgumentException on validation failure.
 */
public final class EngineValidator {

    private EngineValidator() { /* utility */ }

    public static void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        if (config == null) throw new IllegalArgumentException("CodeConfig must not be null");

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
        if (rotorIds == null) throw new IllegalArgumentException("rotorIds must not be null");
        if (positions == null) throw new IllegalArgumentException("positions must not be null");
        if (reflectorId == null) throw new IllegalArgumentException("reflectorId must not be null");
    }

    public static void validateRotorAndPositionCounts(MachineSpec spec, List<Integer> rotorIds, List<Character> positions) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        int required = spec.getRotorsInUse();
        if (rotorIds.size() != required)
            throw new IllegalArgumentException("Exactly " + required + " rotors must be selected");
        if (positions.size() != required)
            throw new IllegalArgumentException("Exactly " + required + " initial positions must be provided");
    }

    public static void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        Set<Integer> seen = new HashSet<>();
        for (int id : rotorIds) {
            if (!seen.add(id)) throw new IllegalArgumentException("Duplicate rotor " + id);
            if (spec.getRotorById(id) == null)
                throw new IllegalArgumentException("Rotor " + id + " does not exist in spec");
        }
    }

    public static void validateReflectorExists(MachineSpec spec, String reflectorId) {
        if (reflectorId.isBlank()) throw new IllegalArgumentException("reflectorId must be non-empty");
        if (spec.getReflectorById(reflectorId) == null)
            throw new IllegalArgumentException("Reflector '" + reflectorId + "' does not exist");
    }

    public static void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions) {
        for (char c : positions) {
            if (!spec.alphabet().contains(c)) throw new IllegalArgumentException(c + " is not a valid position");
        }
    }

    public static void validateInputInAlphabet(MachineSpec spec, String input) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        if (input == null) throw new IllegalArgumentException("Input must not be null");
        for (char c : input.toCharArray()) {
            // Reject control characters explicitly (newline, tab, escape, etc.)
            if (Character.isISOControl(c)) {
                throw new IllegalArgumentException("Invalid character detected: control characters are not allowed (found codepoint: " + (int)c + ")");
            }
            if (!spec.alphabet().contains(c)) {
                throw new IllegalArgumentException(
                    "Invalid character '" + c + "'. All characters must belong to the machine alphabet: " + spec.alphabet().getLetters());
            }
        }
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
     * @throws IllegalArgumentException if validation fails
     */
    private static void validatePlugboard(MachineSpec spec, String plugboard) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        // null or empty plugboard is valid (no plugboard configured)
        if (plugboard == null || plugboard.isEmpty()) {
            return;
        }

        // Check even length
        if (plugboard.length() % 2 != 0) {
            throw new IllegalArgumentException(
                "Plugboard configuration must have even length (pairs of characters), got length " + plugboard.length());
        }

        Set<Character> seenChars = new HashSet<>();

        // Process pairs
        for (int i = 0; i < plugboard.length(); i += 2) {
            char first = plugboard.charAt(i);
            char second = plugboard.charAt(i + 1);

            // Check for self-mapping
            if (first == second) {
                throw new IllegalArgumentException(
                    "Plugboard cannot map a letter to itself: '" + first + first + "'");
            }

            // Check for duplicate characters
            if (!seenChars.add(first)) {
                throw new IllegalArgumentException(
                    "Plugboard letter '" + first + "' appears more than once");
            }
            if (!seenChars.add(second)) {
                throw new IllegalArgumentException(
                    "Plugboard letter '" + second + "' appears more than once");
            }

            // Check characters are in alphabet
            if (!spec.alphabet().contains(first)) {
                throw new IllegalArgumentException(
                    "Plugboard character '" + first + "' is not in the machine alphabet");
            }
            if (!spec.alphabet().contains(second)) {
                throw new IllegalArgumentException(
                    "Plugboard character '" + second + "' is not in the machine alphabet");
            }
        }
    }
}
