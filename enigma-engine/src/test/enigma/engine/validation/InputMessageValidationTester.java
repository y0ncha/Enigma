package test.enigma.engine.validation;

import enigma.engine.EngineValidator;
import enigma.machine.component.alphabet.Alphabet;
import enigma.shared.spec.MachineSpec;
import enigma.shared.spec.ReflectorSpec;
import enigma.shared.spec.RotorSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Manual validation tester for input message validation.
 * Tests all validation scenarios specified in the input message validation requirements.
 */
public class InputMessageValidationTester {

    private static final char ESC_CHAR = '\u001B'; // ESC character (ASCII 27)

    public static void main(String[] args) {
        System.out.println("=== Input Message Validation Tests ===\n");

        MachineSpec mockSpec = createMockMachineSpec();
        int passedTests = 0;
        int totalTests = 0;

        // Test 1: Message includes invalid character (e.g., "&")
        totalTests++;
        System.out.println("Test 1: Message includes invalid character '&'");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC&DEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("&") || e.getMessage().contains("alphabet")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 2: Message includes space (if not in alphabet)
        totalTests++;
        System.out.println("Test 2: Message includes space (not in alphabet ABCD)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC DEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("alphabet") || e.getMessage().contains(" ")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 3: Message includes newline character
        totalTests++;
        System.out.println("Test 3: Message includes newline character");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\nDEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("newline")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 4: Message includes tab character
        totalTests++;
        System.out.println("Test 4: Message includes tab character");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\tDEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("tab")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 5: Message includes ESC character
        totalTests++;
        System.out.println("Test 5: Message includes ESC character (ASCII 27)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC" + ESC_CHAR + "DEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ESC")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 6: Message includes other non-printable character (e.g., ASCII 1)
        totalTests++;
        System.out.println("Test 6: Message includes non-printable character (ASCII 1)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\u0001DEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("non-printable") || e.getMessage().contains("ASCII")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 7: Message includes DEL character (ASCII 127)
        totalTests++;
        System.out.println("Test 7: Message includes DEL character (ASCII 127)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\u007FDEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("non-printable") || e.getMessage().contains("ASCII")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 8: Valid message (should pass)
        totalTests++;
        System.out.println("Test 8: Valid message with only alphabet characters");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABCDABCD");
            System.out.println("  PASSED: Valid message accepted\n");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  FAILED: Valid message should be accepted: " + e.getMessage() + "\n");
        }

        // Test 9: Empty message (should pass - no invalid characters)
        totalTests++;
        System.out.println("Test 9: Empty message");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "");
            System.out.println("  PASSED: Empty message accepted\n");
            passedTests++;
        } catch (Exception e) {
            System.out.println("  FAILED: Empty message should be accepted: " + e.getMessage() + "\n");
        }

        // Test 10: Message with space character (ASCII 32, not in alphabet)
        totalTests++;
        System.out.println("Test 10: Message with space character (ASCII 32, not in alphabet)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "AB CD");
            System.out.println("  FAILED: Should have thrown exception (space not in alphabet)\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("alphabet")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 11: Carriage return (ASCII 13)
        totalTests++;
        System.out.println("Test 11: Message includes carriage return (ASCII 13)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\rDEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("non-printable") || e.getMessage().contains("ASCII")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 12: Form feed (ASCII 12)
        totalTests++;
        System.out.println("Test 12: Message includes form feed (ASCII 12)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\fDEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("non-printable") || e.getMessage().contains("ASCII")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
        }

        // Test 13: Null character (ASCII 0)
        totalTests++;
        System.out.println("Test 13: Message includes null character (ASCII 0)");
        try {
            EngineValidator.validateInputInAlphabet(mockSpec, "ABC\u0000DEF");
            System.out.println("  FAILED: Should have thrown exception\n");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("non-printable") || e.getMessage().contains("ASCII")) {
                System.out.println("  PASSED: " + e.getMessage() + "\n");
                passedTests++;
            } else {
                System.out.println("  FAILED: Wrong exception message: " + e.getMessage() + "\n");
            }
        } catch (Exception e) {
            System.out.println("  FAILED: Wrong exception type: " + e.getClass().getName() + "\n");
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
        rotors.put(1, new RotorSpec(1, 0, new char[]{'A', 'B', 'C', 'D'}, new char[]{'B', 'A', 'D', 'C'}));
        rotors.put(2, new RotorSpec(2, 1, new char[]{'A', 'B', 'C', 'D'}, new char[]{'C', 'D', 'A', 'B'}));
        rotors.put(3, new RotorSpec(3, 2, new char[]{'A', 'B', 'C', 'D'}, new char[]{'D', 'C', 'B', 'A'}));

        // Create mock reflectors (minimal spec needed for validation)
        Map<String, ReflectorSpec> reflectors = new HashMap<>();
        reflectors.put("I", new ReflectorSpec("I", new int[]{1, 0, 3, 2}));
        reflectors.put("II", new ReflectorSpec("II", new int[]{2, 3, 0, 1}));

        // include rotorsInUse (3) to match MachineSpec record constructor
        return new MachineSpec(alphabet, rotors, reflectors, 3);
    }
}
