package test.enigma.console.helper;

import enigma.console.helper.InputParsers;

import java.util.List;

/**
 * Manual tester for InputParsers utility class.
 * 
 * <p>Tests the parsing and conversion methods used for console input processing:
 * <ul>
 *   <li>Rotor ID parsing from comma-separated strings</li>
 *   <li>Integer to Roman numeral conversion</li>
 *   <li>Initial position parsing from letter strings</li>
 * </ul>
 */
public class InputParsersTester {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("========================================");
        System.out.println(" InputParsers Test Suite");
        System.out.println("========================================\n");

        // Test parseRotorIds
        boolean test1 = testParseRotorIds();
        passed += test1 ? 1 : 0;
        failed += test1 ? 0 : 1;

        // Test toRoman
        boolean test2 = testToRoman();
        passed += test2 ? 1 : 0;
        failed += test2 ? 0 : 1;

        // Test buildInitialPositions
        boolean test3 = testBuildInitialPositions();
        passed += test3 ? 1 : 0;
        failed += test3 ? 0 : 1;

        // Test error cases
        boolean test4 = testParseRotorIdsErrors();
        passed += test4 ? 1 : 0;
        failed += test4 ? 0 : 1;

        boolean test5 = testBuildInitialPositionsErrors();
        passed += test5 ? 1 : 0;
        failed += test5 ? 0 : 1;

        // Summary
        System.out.println("\n=========== SUMMARY ===========");
        System.out.println("Total tests : " + (passed + failed));
        System.out.println("Passed      : " + passed);
        System.out.println("Failed      : " + failed);
        if (failed == 0) {
            System.out.println("Status      : ALL GOOD ✔✔✔");
        } else {
            System.out.println("Status      : CHECK FAILED CASES ✘");
        }
        System.out.println("================================");
    }

    private static boolean testParseRotorIds() {
        System.out.println("========== Test: parseRotorIds ==========");
        boolean allPassed = true;

        // Test case 1: simple comma-separated IDs
        List<Integer> result1 = InputParsers.parseRotorIds("1,2,3");
        boolean test1 = result1.equals(List.of(1, 2, 3));
        System.out.println("Input: \"1,2,3\" -> Expected: [1, 2, 3], Actual: " + result1 + " " + (test1 ? "✔" : "✘"));
        allPassed &= test1;

        // Test case 2: IDs with spaces
        List<Integer> result2 = InputParsers.parseRotorIds("10, 20, 30");
        boolean test2 = result2.equals(List.of(10, 20, 30));
        System.out.println("Input: \"10, 20, 30\" -> Expected: [10, 20, 30], Actual: " + result2 + " " + (test2 ? "✔" : "✘"));
        allPassed &= test2;

        // Test case 3: single ID
        List<Integer> result3 = InputParsers.parseRotorIds("42");
        boolean test3 = result3.equals(List.of(42));
        System.out.println("Input: \"42\" -> Expected: [42], Actual: " + result3 + " " + (test3 ? "✔" : "✘"));
        allPassed &= test3;

        // Test case 4: empty parts should be skipped
        List<Integer> result4 = InputParsers.parseRotorIds("1,,3");
        boolean test4 = result4.equals(List.of(1, 3));
        System.out.println("Input: \"1,,3\" -> Expected: [1, 3], Actual: " + result4 + " " + (test4 ? "✔" : "✘"));
        allPassed &= test4;

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }

    private static boolean testToRoman() {
        System.out.println("========== Test: toRoman ==========");
        boolean allPassed = true;

        // Test cases for Roman numeral conversion
        String[][] testCases = {
            {"1", "I"},
            {"2", "II"},
            {"3", "III"},
            {"4", "IV"},
            {"5", "V"},
            {"6", "?6"},  // beyond standard range
            {"10", "?10"} // beyond standard range
        };

        for (String[] testCase : testCases) {
            int input = Integer.parseInt(testCase[0]);
            String expected = testCase[1];
            String actual = InputParsers.toRoman(input);
            boolean passed = actual.equals(expected);
            System.out.println("Input: " + input + " -> Expected: \"" + expected + "\", Actual: \"" + actual + "\" " + (passed ? "✔" : "✘"));
            allPassed &= passed;
        }

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }

    private static boolean testBuildInitialPositions() {
        System.out.println("========== Test: buildInitialPositions ==========");
        boolean allPassed = true;

        // Test case 1: "ABC" -> ['A', 'B', 'C'] (left to right)
        // First char 'A' corresponds to LEFTMOST rotor, 'C' to rightmost
        List<Character> result1 = InputParsers.buildInitialPositions("ABC");
        boolean test1 = result1.equals(List.of('A', 'B', 'C'));
        System.out.println("Input: \"ABC\" -> Expected: ['A', 'B', 'C'], Actual: " + result1 + " " + (test1 ? "✔" : "✘"));
        allPassed &= test1;

        // Test case 2: "CCC"
        List<Character> result2 = InputParsers.buildInitialPositions("CCC");
        boolean test2 = result2.equals(List.of('C', 'C', 'C'));
        System.out.println("Input: \"CCC\" -> Expected: ['C', 'C', 'C'], Actual: " + result2 + " " + (test2 ? "✔" : "✘"));
        allPassed &= test2;

        // Test case 3: single letter
        List<Character> result3 = InputParsers.buildInitialPositions("Z");
        boolean test3 = result3.equals(List.of('Z'));
        System.out.println("Input: \"Z\" -> Expected: ['Z'], Actual: " + result3 + " " + (test3 ? "✔" : "✘"));
        allPassed &= test3;

        // Test case 4: lowercase should work (case-insensitive)
        List<Character> result4 = InputParsers.buildInitialPositions("abc");
        boolean test4 = result4.equals(List.of('A', 'B', 'C'));
        System.out.println("Input: \"abc\" -> Expected: ['A', 'B', 'C'], Actual: " + result4 + " " + (test4 ? "✔" : "✘"));
        allPassed &= test4;

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }

    private static boolean testParseRotorIdsErrors() {
        System.out.println("========== Test: parseRotorIds Error Cases ==========");
        boolean allPassed = true;

        // Test case 1: non-numeric input should throw IllegalArgumentException
        try {
            InputParsers.parseRotorIds("1,abc,3");
            System.out.println("Input: \"1,abc,3\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
            allPassed = false;
        } catch (IllegalArgumentException e) {
            System.out.println("Input: \"1,abc,3\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
        }

        // Test case 2: mixed valid and invalid
        try {
            InputParsers.parseRotorIds("1,2,three");
            System.out.println("Input: \"1,2,three\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
            allPassed = false;
        } catch (IllegalArgumentException e) {
            System.out.println("Input: \"1,2,three\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
        }

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }

    private static boolean testBuildInitialPositionsErrors() {
        System.out.println("========== Test: buildInitialPositions Error Cases ==========");
        boolean allPassed = true;

        // Test case 1: non-letter character should throw IllegalArgumentException
        try {
            InputParsers.buildInitialPositions("A1C");
            System.out.println("Input: \"A1C\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
            allPassed = false;
        } catch (IllegalArgumentException e) {
            System.out.println("Input: \"A1C\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
        }

        // Test case 2: special characters
        try {
            InputParsers.buildInitialPositions("A@C");
            System.out.println("Input: \"A@C\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
            allPassed = false;
        } catch (IllegalArgumentException e) {
            System.out.println("Input: \"A@C\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
        }

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }
}
