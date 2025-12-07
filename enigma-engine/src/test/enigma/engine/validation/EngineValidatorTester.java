package test.enigma.engine.validation;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
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
        Engine engine = new EngineImpl();

        int passedTests = 0;
        int totalTests = 0;

        // Test 1: Wrong rotor count (2 rotors)
        totalTests++;
        System.out.println("Test 1: Providing 2 rotors instead of 3");
        try {
            engine.validateRotorAndPositionCounts(List.of(1, 2), List.of('A', 'A'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 2: Wrong rotor count (4 rotors)
        totalTests++;
        System.out.println("Test 2: Providing 4 rotors instead of 3");
        try {
            engine.validateRotorAndPositionCounts(List.of(1, 2, 3, 4), List.of('A', 'A', 'A', 'A'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 3: Duplicate rotor IDs
        totalTests++;
        System.out.println("Test 3: Duplicate rotor IDs (1, 2, 2)");
        try {
            engine.validateRotorIdsExistenceAndUniqueness(mockSpec, List.of(1, 2, 2));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 4: Rotor ID out of range
        totalTests++;
        System.out.println("Test 4: Rotor ID 99 does not exist");
        try {
            engine.validateRotorIdsExistenceAndUniqueness(mockSpec, List.of(1, 2, 99));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 5: Position string wrong length
        totalTests++;
        System.out.println("Test 5: Position string wrong length (2 positions for 3 rotors)");
        try {
            engine.validateRotorAndPositionCounts(List.of(1, 2, 3), List.of('A', 'B'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 6: Position char not in alphabet
        totalTests++;
        System.out.println("Test 6: Position character 'Z' not in alphabet (ABCD)");
        try {
            engine.validatePositionsInAlphabet(mockSpec, List.of('A', 'B', 'Z'));
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 7: Invalid reflector (doesn't exist in spec)
        totalTests++;
        System.out.println("Test 7: Invalid reflector 'III' (not in spec which has I, II)");
        try {
            engine.validateReflectorExists(mockSpec, "III");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 8: Invalid reflector (another ID not in spec)
        totalTests++;
        System.out.println("Test 8: Invalid reflector 'INVALID' (not in spec)");
        try {
            engine.validateReflectorExists(mockSpec, "INVALID");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 9: Plugboard odd length
        totalTests++;
        System.out.println("Test 9: Plugboard odd length (ABC)");
        try {
            engine.validatePlugboard(mockSpec, "ABC");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 10: Plugboard letter appears twice
        totalTests++;
        System.out.println("Test 10: Plugboard letter 'A' appears twice (ABAC)");
        try {
            engine.validatePlugboard(mockSpec, "ABAC");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 11: Plugboard self-mapping (A→A)
        totalTests++;
        System.out.println("Test 11: Plugboard self-mapping (AA)");
        try {
            engine.validatePlugboard(mockSpec, "AA");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 12: Plugboard character not in alphabet
        totalTests++;
        System.out.println("Test 12: Plugboard character 'Z' not in alphabet (ABCD)");
        try {
            engine.validatePlugboard(mockSpec, "AZ");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            System.out.println("  PASSED: " + e.getMessage() + "\n");
            passedTests++;
        }

        // Test 13: Valid plugboard (should pass)
        totalTests++;
        System.out.println("Test 13: Valid plugboard (AB)");
        try {
            engine.validatePlugboard(mockSpec, "AB");
            System.out.println("  PASSED: Valid plugboard accepted\n");
            passedTests++;
        } catch (IllegalArgumentException e) {
            System.out.println("  FAILED: Valid plugboard should be accepted: " + e.getMessage() + "\n");
        }

        // Test 14: Empty/null plugboard (should pass)
        totalTests++;
        System.out.println("Test 14: Null plugboard (no plugboard)");
        try {
            engine.validatePlugboard(mockSpec, null);
            System.out.println("  PASSED: Null plugboard accepted\n");
            passedTests++;
        } catch (IllegalArgumentException e) {
            System.out.println("  FAILED: Null plugboard should be accepted: " + e.getMessage() + "\n");
        }

        // Test 15: Valid configuration (should pass)
        totalTests++;
        System.out.println("Test 15: Valid complete configuration");
        try {
            CodeConfig validConfig = new CodeConfig(
                List.of(1, 2, 3),
                List.of('A', 'B', 'C'),
                "I"
            );
            engine.validateCodeConfig(mockSpec, validConfig);
            System.out.println("  PASSED: Valid configuration accepted\n");
            passedTests++;
        } catch (IllegalArgumentException e) {
            System.out.println("  FAILED: Valid configuration should be accepted: " + e.getMessage() + "\n");
        }

        // Summary
        System.out.println("===========================================");
        System.out.println("Test Results: " + passedTests + "/" + totalTests + " passed");
        if (passedTests == totalTests) {
            System.out.println("All tests PASSED! ✓");
        } else {
            System.out.println((totalTests - passedTests) + " test(s) FAILED! ✗");
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
        rotors.put(1, new RotorSpec(1, new int[]{1, 0, 3, 2}, 'A'));  // Mock rotor 1
        rotors.put(2, new RotorSpec(2, new int[]{2, 3, 0, 1}, 'B'));  // Mock rotor 2
        rotors.put(3, new RotorSpec(3, new int[]{3, 2, 1, 0}, 'C'));  // Mock rotor 3

        // Create mock reflectors (minimal spec needed for validation)
        Map<String, ReflectorSpec> reflectors = new HashMap<>();
        reflectors.put("I", new ReflectorSpec("I", new int[]{1, 0, 3, 2}));   // Mock reflector I
        reflectors.put("II", new ReflectorSpec("II", new int[]{2, 3, 0, 1})); // Mock reflector II

        return new MachineSpec(alphabet, rotors, reflectors);
    }
}
