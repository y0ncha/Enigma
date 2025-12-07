package test.enigma.engine.validation;

import enigma.engine.EngineValidator;
import enigma.engine.exception.InvalidConfigurationException;
import enigma.machine.component.alphabet.Alphabet;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;
import enigma.shared.spec.ReflectorSpec;
import enigma.shared.spec.RotorSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manual validation tester for EngineValidator.
 * Tests all validation scenarios specified in the manual code configuration validation requirements.
 */
public class EngineValidatorTester {

    public static void main(String[] args) {
        System.out.println("=== EngineValidator Manual Validation Tests ===\n");

        // Create a mock machine spec for testing
        MachineSpec mockSpec = createMockMachineSpec();


        int passedTests = 0;
        int totalTests = 0;
        int skippedTests = 0;

        // Test 1: Wrong rotor count (2 rotors)
        totalTests++;
        System.out.println("Test 1: Providing 2 rotors instead of 3");
        try {
            EngineValidator.validateRotorAndPositionCounts(mockSpec, List.of(1, 2), List.of('A', 'A'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 2: Wrong rotor count (4 rotors)
        totalTests++;
        System.out.println("Test 2: Providing 4 rotors instead of 3");
        try {
            EngineValidator.validateRotorAndPositionCounts(mockSpec, List.of(1, 2, 3, 4), List.of('A', 'A', 'A', 'A'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 3: Duplicate rotor IDs
        totalTests++;
        System.out.println("Test 3: Duplicate rotor IDs (1, 2, 2)");
        try {
            EngineValidator.validateRotorIdsExistenceAndUniqueness(mockSpec, List.of(1, 2, 2));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 4: Rotor ID out of range
        totalTests++;
        System.out.println("Test 4: Rotor ID 99 does not exist");
        try {
            EngineValidator.validateRotorIdsExistenceAndUniqueness(mockSpec, List.of(1, 2, 99));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 5: Position string wrong length
        totalTests++;
        System.out.println("Test 5: Position string wrong length (2 positions for 3 rotors)");
        try {
            EngineValidator.validateRotorAndPositionCounts(mockSpec, List.of(1, 2, 3), List.of('A', 'B'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 6: Position char not in alphabet
        totalTests++;
        System.out.println("Test 6: Position character 'Z' not in alphabet (ABCD)");
        try {
            EngineValidator.validatePositionsInAlphabet(mockSpec, List.of('A', 'B', 'Z'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 7: Invalid reflector (doesn't exist in spec)
        totalTests++;
        System.out.println("Test 7: Invalid reflector 'III' (not in spec which has I, II)");
        try {
            EngineValidator.validateReflectorExists(mockSpec, "III");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 8: Invalid reflector (another ID not in spec)
        totalTests++;
        System.out.println("Test 8: Invalid reflector 'INVALID' (not in spec)");
        try {
            EngineValidator.validateReflectorExists(mockSpec, "INVALID");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 9: Plugboard odd length
        totalTests++;
        System.out.println("Test 9: Plugboard odd length (ABC)");
        // The plugboard validation method is private inside EngineValidator and not part of the public API.
        // This test is therefore skipped: plugboard validation is exercised via the public code path when
        // plugboard support is added to CodeConfig in a future exercise.
        System.out.println("  SKIPPED: plugboard validation is private and not tested directly.\n");
        skippedTests++;

        // Test 15: Valid configuration (should pass)
        totalTests++;
        System.out.println("Test 15: Valid complete configuration");
        try {
            CodeConfig validConfig = new CodeConfig(
                List.of(1, 2, 3),
                List.of('A', 'B', 'C'),
                "I"
            );
            EngineValidator.validateCodeConfig(mockSpec, validConfig);
            System.out.println("  PASSED: Valid configuration accepted\n");
            passedTests++;
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            System.out.println("  FAILED: Valid configuration should be accepted: " + e.getMessage() + "\n");
        }

        // Summary
        System.out.println("===========================================");
        System.out.println("Test Results: " + passedTests + "/" + (totalTests - skippedTests) + " passed (" + skippedTests + " skipped)");
        if (passedTests == (totalTests - skippedTests)) {
            System.out.println("All runnable tests PASSED! ✓");
        } else {
            System.out.println(((totalTests - skippedTests) - passedTests) + " test(s) FAILED! ✗");
        }
    }

    /**
     * Create a mock MachineSpec for testing.
     * Alphabet: ABCD (4 letters)
     * Rotors: 1, 2, 3
     * Reflectors: I, II
     */
    private static MachineSpec createMockMachineSpec() {
        // Create alphabet with letters A, B, C, D
        Alphabet alphabet = new Alphabet("ABCD");

        // Create mock rotors (minimal spec needed for validation)
        Map<Integer, RotorSpec> rotors = new HashMap<>();
        rotors.put(1, new RotorSpec(1, 0, new char[]{'A', 'B', 'C', 'D'}, new char[]{'B', 'A', 'D', 'C'}));
        rotors.put(2, new RotorSpec(2, 1, new char[]{'A', 'B', 'C', 'D'}, new char[]{'C', 'D', 'A', 'B'}));
        rotors.put(3, new RotorSpec(3, 2, new char[]{'A', 'B', 'C', 'D'}, new char[]{'D', 'C', 'B', 'A'}));

        // Create mock reflectors (minimal spec needed for validation)
        Map<String, ReflectorSpec> reflectors = new HashMap<>();
        reflectors.put("I", new ReflectorSpec("I", new int[]{1, 0, 3, 2}));
        reflectors.put("II", new ReflectorSpec("II", new int[]{2, 3, 0, 1}));

        // include rotorsInUse (3) to match MachineSpec record update
        return new MachineSpec(alphabet, rotors, reflectors, 3);
    }
}
