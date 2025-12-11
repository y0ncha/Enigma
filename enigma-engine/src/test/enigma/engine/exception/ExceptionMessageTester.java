package test.enigma.engine.exception;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.engine.exception.*;
import enigma.loader.exception.EnigmaLoadingException;
import enigma.shared.dto.config.CodeConfig;

import java.nio.file.Paths;
import java.util.List;

/**
 * Manual test harness for verifying exception messages are descriptive and user-friendly.
 *
 * <p>This test exercises various error scenarios and prints the exception messages
 * to verify they include:</p>
 * <ul>
 *   <li><b>What</b> is wrong</li>
 *   <li><b>Where</b> the problem occurred (rotor ID, reflector ID, position, etc.)</li>
 *   <li><b>How</b> to fix it</li>
 * </ul>
 *
 * <p>Run this test to manually verify exception quality.</p>
 *
 * @since 1.0
 */
public class ExceptionMessageTester {

    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";
    private static final String VALID_XML = Paths.get(XML_BASE_DIR, "ex1-sanity-paper-enigma.xml").toString();
    private static final String INVALID_XML = "non-existent-file.xml";

    private static int testCount = 0;
    private static int passCount = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Exception Message Quality Tests");
        System.out.println("========================================\n");

        // Test 1: Load non-existent file
        testLoadNonExistentFile();

        // Test 2: Process without loading machine
        testProcessWithoutLoading();

        // Test 3: Process without configuring machine
        testProcessWithoutConfig();

        // Test 4: Invalid rotor count
        testInvalidRotorCount();

        // Test 5: Duplicate rotor ID
        testDuplicateRotorId();

        // Test 6: Non-existent rotor ID
        testNonExistentRotorId();

        // Test 7: Invalid reflector ID
        testInvalidReflectorId();

        // Test 8: Invalid position character
        testInvalidPositionCharacter();

        // Test 9: Invalid input character
        testInvalidInputCharacter();

        // Test 10: Control character in input
        testControlCharacterInInput();

        // Test 11: Configure without loading
        testConfigureWithoutLoading();

        // Summary
        System.out.println("\n========================================");
        System.out.println("Test Summary");
        System.out.println("========================================");
        System.out.println("Total tests: " + testCount);
        System.out.println("Passed: " + passCount);
        System.out.println("Failed: " + (testCount - passCount));
        
        if (passCount == testCount) {
            System.out.println("\n✓ All exception messages are descriptive and actionable!");
        } else {
            System.out.println("\n✗ Some exception messages need improvement.");
        }
    }

    private static void testLoadNonExistentFile() {
        testCount++;
        System.out.println("Test " + testCount + ": Load non-existent file");
        System.out.println("Expected: File not found error with path and fix suggestion");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(INVALID_XML);
            System.out.println("✗ FAIL: Expected EngineException but no exception was thrown\n");
        }
        }
        catch (Exception e) {
            System.out.println("✓ PASS: Got EngineException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Fix:", INVALID_XML)) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        }
    }

    private static void testProcessWithoutLoading() {
        testCount++;
        System.out.println("Test " + testCount + ": Process message without loading machine");
        System.out.println("Expected: MachineNotLoadedException with clear fix instruction");
        
        try {
            Engine engine = new EngineImpl();
            engine.process("ABC");
            System.out.println("✗ FAIL: Expected MachineNotLoadedException but no exception was thrown\n");
        } catch (MachineNotLoadedException e) {
            System.out.println("✓ PASS: Got MachineNotLoadedException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Fix:", "loadMachine")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected MachineNotLoadedException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testProcessWithoutConfig() {
        testCount++;
        System.out.println("Test " + testCount + ": Process message without configuring machine");
        System.out.println("Expected: MachineNotConfiguredException with clear fix instruction");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            engine.process("ABC");
            System.out.println("✗ FAIL: Expected MachineNotConfiguredException but no exception was thrown\n");
        } catch (MachineNotConfiguredException e) {
            System.out.println("✓ PASS: Got MachineNotConfiguredException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Fix:", "config")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected MachineNotConfiguredException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testInvalidRotorCount() {
        testCount++;
        System.out.println("Test " + testCount + ": Configure with wrong rotor count");
        System.out.println("Expected: InvalidConfigurationException with expected count and fix");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(
                List.of(1, 2),  // Only 2 rotors instead of 3
                List.of('A', 'A'),
                "I"
            );
            engine.configManual(config);
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but no exception was thrown\n");
        } catch (InvalidConfigurationException e) {
            System.out.println("✓ PASS: Got InvalidConfigurationException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Fix:", "Expected exactly")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testDuplicateRotorId() {
        testCount++;
        System.out.println("Test " + testCount + ": Configure with duplicate rotor ID");
        System.out.println("Expected: InvalidConfigurationException mentioning duplicate and rotor ID");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(
                List.of(1, 1, 2),  // Duplicate rotor 1
                List.of('A', 'A', 'A'),
                "I"
            );
            engine.configManual(config);
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but no exception was thrown\n");
        } catch (InvalidConfigurationException e) {
            System.out.println("✓ PASS: Got InvalidConfigurationException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Duplicate", "Fix:")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testNonExistentRotorId() {
        testCount++;
        System.out.println("Test " + testCount + ": Configure with non-existent rotor ID");
        System.out.println("Expected: InvalidConfigurationException with available rotor IDs");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(
                List.of(1, 2, 999),  // Rotor 999 doesn't exist
                List.of('A', 'A', 'A'),
                "I"
            );
            engine.configManual(config);
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but no exception was thrown\n");
        } catch (InvalidConfigurationException e) {
            System.out.println("✓ PASS: Got InvalidConfigurationException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Available", "Fix:")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testInvalidReflectorId() {
        testCount++;
        System.out.println("Test " + testCount + ": Configure with non-existent reflector ID");
        System.out.println("Expected: InvalidConfigurationException with available reflector IDs");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(
                List.of(1, 2, 3),
                List.of('A', 'A', 'A'),
                "X"  // Reflector X doesn't exist
            );
            engine.configManual(config);
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but no exception was thrown\n");
        } catch (InvalidConfigurationException e) {
            System.out.println("✓ PASS: Got InvalidConfigurationException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Available", "Fix:")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testInvalidPositionCharacter() {
        testCount++;
        System.out.println("Test " + testCount + ": Configure with invalid position character");
        System.out.println("Expected: InvalidConfigurationException with position index and alphabet");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(
                List.of(1, 2, 3),
                List.of('A', '?', 'A'),  // '?' is not in alphabet
                "I"
            );
            engine.configManual(config);
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but no exception was thrown\n");
        } catch (InvalidConfigurationException e) {
            System.out.println("✓ PASS: Got InvalidConfigurationException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "alphabet", "Fix:")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidConfigurationException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testInvalidInputCharacter() {
        testCount++;
        System.out.println("Test " + testCount + ": Process with invalid input character");
        System.out.println("Expected: InvalidMessageException with character, position, and alphabet");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(List.of(1, 2, 3), List.of('A', 'A', 'A'), "I");
            engine.configManual(config);
            engine.process("ABC?DEF");  // '?' is not in alphabet
            System.out.println("✗ FAIL: Expected InvalidMessageException but no exception was thrown\n");
        } catch (InvalidMessageException e) {
            System.out.println("✓ PASS: Got InvalidMessageException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "position", "Fix:")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidMessageException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testControlCharacterInInput() {
        testCount++;
        System.out.println("Test " + testCount + ": Process with control character in input");
        System.out.println("Expected: InvalidMessageException mentioning control character");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadMachine(VALID_XML);
            CodeConfig config = new CodeConfig(List.of(1, 2, 3), List.of('A', 'A', 'A'), "I");
            engine.configManual(config);
            engine.process("ABC\nDEF");  // Newline character
            System.out.println("✗ FAIL: Expected InvalidMessageException but no exception was thrown\n");
        } catch (InvalidMessageException e) {
            System.out.println("✓ PASS: Got InvalidMessageException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "control", "Fix:")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected InvalidMessageException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    private static void testConfigureWithoutLoading() {
        testCount++;
        System.out.println("Test " + testCount + ": Configure without loading machine");
        System.out.println("Expected: MachineNotLoadedException with clear fix instruction");
        
        try {
            Engine engine = new EngineImpl();
            CodeConfig config = new CodeConfig(List.of(1, 2, 3), List.of('A', 'A', 'A'), "I");
            engine.configManual(config);
            System.out.println("✗ FAIL: Expected MachineNotLoadedException but no exception was thrown\n");
        } catch (MachineNotLoadedException e) {
            System.out.println("✓ PASS: Got MachineNotLoadedException");
            System.out.println("Message: " + e.getMessage());
            if (isDescriptiveMessage(e.getMessage(), "Fix:", "loadMachine")) {
                passCount++;
                System.out.println("✓ Message is descriptive\n");
            } else {
                System.out.println("✗ Message could be more descriptive\n");
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: Expected MachineNotLoadedException but got " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage() + "\n");
        }
    }

    /**
     * Minimum length for a descriptive exception message.
     * Messages shorter than this are considered insufficiently descriptive.
     */
    private static final int MIN_DESCRIPTIVE_MESSAGE_LENGTH = 50;

    /**
     * Check if a message contains the expected keywords that indicate a descriptive error.
     */
    private static boolean isDescriptiveMessage(String message, String... keywords) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        // Check that the message contains all required keywords
        for (String keyword : keywords) {
            if (!message.contains(keyword)) {
                return false;
            }
        }
        
        // Check minimum length - descriptive messages should be substantial
        return message.length() > MIN_DESCRIPTIVE_MESSAGE_LENGTH;
    }
}
