package test.enigma.console.helper;

import enigma.console.helper.InputParsers;

import java.util.List;

/**
 * Manual test harness for {@link InputParsers}.
 *
 * <p>Tests the parsing and validation logic in the InputParsers utility class,
 * including valid and invalid input handling. This is not a unit test but a
 * manual verification tool.</p>
 *
 * @since 1.0
 */
public class InputParsersTester {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    /**
     * Entry point for the test harness.
     */
    public static void main(String[] args) {
        System.out.println("==============================");
        System.out.println("InputParsers Test Harness");
        System.out.println("==============================\n");

        testParseRotorIds();
        testToRoman();
        testBuildInitialPositions();

        System.out.println("\n==============================");
        System.out.println("Test Summary");
        System.out.println("==============================");
        System.out.println("Total tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        System.out.println("==============================");
    }

    private static void testParseRotorIds() {
        System.out.println("Testing parseRotorIds():");
        System.out.println("------------------------");

        // Valid inputs
        testParseRotorIdsValid("1,2,3", List.of(1, 2, 3));
        testParseRotorIdsValid("5,4,3,2,1", List.of(5, 4, 3, 2, 1));
        testParseRotorIdsValid("1", List.of(1));
        testParseRotorIdsValid("10,20,30", List.of(10, 20, 30));
        testParseRotorIdsValid(" 1 , 2 , 3 ", List.of(1, 2, 3)); // with spaces
        testParseRotorIdsValid("1,,2", List.of(1, 2)); // empty parts should be skipped
        testParseRotorIdsValid("1, , 2", List.of(1, 2)); // space-only parts are also skipped after trim

        // Invalid inputs - should throw IllegalArgumentException
        testParseRotorIdsInvalid("1,a,3", "non-numeric value");
        testParseRotorIdsInvalid("1,2.5,3", "decimal number");
        testParseRotorIdsInvalid("abc", "non-numeric string");
        testParseRotorIdsInvalid("1,2,three", "mixed numeric and text");

        System.out.println();
    }

    private static void testParseRotorIdsValid(String input, List<Integer> expected) {
        testsRun++;
        try {
            List<Integer> result = InputParsers.parseRotorIds(input);
            if (result.equals(expected)) {
                System.out.println("✓ PASS: parseRotorIds(\"" + input + "\") = " + result);
                testsPassed++;
            } else {
                System.out.println("✗ FAIL: parseRotorIds(\"" + input + "\")");
                System.out.println("  Expected: " + expected);
                System.out.println("  Got: " + result);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: parseRotorIds(\"" + input + "\") threw " + e.getClass().getSimpleName());
            System.out.println("  Expected: " + expected);
            System.out.println("  Exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testParseRotorIdsInvalid(String input, String description) {
        testsRun++;
        try {
            List<Integer> result = InputParsers.parseRotorIds(input);
            System.out.println("✗ FAIL: parseRotorIds(\"" + input + "\") (" + description + ")");
            System.out.println("  Expected: IllegalArgumentException");
            System.out.println("  Got: " + result);
            testsFailed++;
        } catch (IllegalArgumentException e) {
            System.out.println("✓ PASS: parseRotorIds(\"" + input + "\") correctly threw IllegalArgumentException");
            System.out.println("  Message: " + e.getMessage());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAIL: parseRotorIds(\"" + input + "\") (" + description + ")");
            System.out.println("  Expected: IllegalArgumentException");
            System.out.println("  Got: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testToRoman() {
        System.out.println("Testing toRoman():");
        System.out.println("------------------");

        // Valid roman numeral conversions
        testToRomanValid(1, "I");
        testToRomanValid(2, "II");
        testToRomanValid(3, "III");
        testToRomanValid(4, "IV");
        testToRomanValid(5, "V");

        // Values outside the standard range - should return "?<value>"
        testToRomanValid(0, "?0");
        testToRomanValid(6, "?6");
        testToRomanValid(10, "?10");
        testToRomanValid(-1, "?-1");
        testToRomanValid(100, "?100");

        System.out.println();
    }

    private static void testToRomanValid(int input, String expected) {
        testsRun++;
        try {
            String result = InputParsers.toRoman(input);
            if (result.equals(expected)) {
                System.out.println("✓ PASS: toRoman(" + input + ") = \"" + result + "\"");
                testsPassed++;
            } else {
                System.out.println("✗ FAIL: toRoman(" + input + ")");
                System.out.println("  Expected: \"" + expected + "\"");
                System.out.println("  Got: \"" + result + "\"");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: toRoman(" + input + ") threw " + e.getClass().getSimpleName());
            System.out.println("  Expected: \"" + expected + "\"");
            System.out.println("  Exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testBuildInitialPositions() {
        System.out.println("Testing buildInitialPositions():");
        System.out.println("--------------------------------");

        // Valid inputs
        testBuildInitialPositionsValid("ABC", List.of(2, 1, 0)); // Rightmost first
        testBuildInitialPositionsValid("A", List.of(0));
        testBuildInitialPositionsValid("AAA", List.of(0, 0, 0));
        testBuildInitialPositionsValid("XYZ", List.of(25, 24, 23));
        testBuildInitialPositionsValid("abc", List.of(2, 1, 0)); // lowercase should work
        testBuildInitialPositionsValid("AbC", List.of(2, 1, 0)); // mixed case
        testBuildInitialPositionsValid("Z", List.of(25));
        testBuildInitialPositionsValid("ABCDEFGHIJ", List.of(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)); // longer input
        testBuildInitialPositionsValid("", List.of()); // empty string produces empty list

        // Invalid inputs - should throw IllegalArgumentException
        testBuildInitialPositionsInvalid("AB1", "contains digit");
        testBuildInitialPositionsInvalid("A B", "contains space");
        testBuildInitialPositionsInvalid("A-B", "contains hyphen");
        testBuildInitialPositionsInvalid("123", "all digits");
        testBuildInitialPositionsInvalid("A@B", "contains special character");

        System.out.println();
    }

    private static void testBuildInitialPositionsValid(String input, List<Integer> expected) {
        testsRun++;
        try {
            List<Integer> result = InputParsers.buildInitialPositions(input);
            if (result.equals(expected)) {
                System.out.println("✓ PASS: buildInitialPositions(\"" + input + "\") = " + result);
                testsPassed++;
            } else {
                System.out.println("✗ FAIL: buildInitialPositions(\"" + input + "\")");
                System.out.println("  Expected: " + expected);
                System.out.println("  Got: " + result);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("✗ FAIL: buildInitialPositions(\"" + input + "\") threw " + e.getClass().getSimpleName());
            System.out.println("  Expected: " + expected);
            System.out.println("  Exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testBuildInitialPositionsInvalid(String input, String description) {
        testsRun++;
        try {
            List<Integer> result = InputParsers.buildInitialPositions(input);
            System.out.println("✗ FAIL: buildInitialPositions(\"" + input + "\") (" + description + ")");
            System.out.println("  Expected: IllegalArgumentException");
            System.out.println("  Got: " + result);
            testsFailed++;
        } catch (IllegalArgumentException e) {
            System.out.println("✓ PASS: buildInitialPositions(\"" + input + "\") correctly threw IllegalArgumentException");
            System.out.println("  Message: " + e.getMessage());
            testsPassed++;
        } catch (Exception e) {
            System.out.println("✗ FAIL: buildInitialPositions(\"" + input + "\") (" + description + ")");
            System.out.println("  Expected: IllegalArgumentException");
            System.out.println("  Got: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            testsFailed++;
        }
    }
}
