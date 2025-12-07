package enigma.engine;

import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * EngineValidator — stateless validation helpers for engine configuration.
 *
 * One-line: validate a CodeConfig against a MachineSpec.
 *
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

    private static final int ROTORS_IN_USE = 3; // keep in sync with EngineImpl

    private EngineValidator() { /* utility */ }

    public static void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        if (config == null) throw new IllegalArgumentException("CodeConfig must not be null");

        List<Integer> rotorIds = config.rotorIds();
        List<Character> positions = config.positions();
        String reflectorId = config.reflectorId();

        validateNullChecks(rotorIds, positions, reflectorId);
        validateRotorAndPositionCounts(rotorIds, positions);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
        validateReflectorExists(spec, reflectorId);
        validatePositionsInAlphabet(spec, positions);
    }

    public static void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId) {
        if (rotorIds == null) throw new IllegalArgumentException("rotorIds must not be null");
        if (positions == null) throw new IllegalArgumentException("positions must not be null");
        if (reflectorId == null) throw new IllegalArgumentException("reflectorId must not be null");
    }

    public static void validateRotorAndPositionCounts(List<Integer> rotorIds, List<Character> positions) {
        if (rotorIds.size() != ROTORS_IN_USE)
            throw new IllegalArgumentException("Exactly " + ROTORS_IN_USE + " rotors must be selected");
        if (positions.size() != ROTORS_IN_USE)
            throw new IllegalArgumentException("Exactly " + ROTORS_IN_USE + " initial positions must be provided");
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
        for (char c : input.toCharArray()) {
            if (!spec.alphabet().contains(c)) {
                throw new IllegalArgumentException(
                    "Invalid character '" + c + "'. All characters must belong to the machine alphabet: " + spec.alphabet().getLetters());
            }
        }
    }
}

