//package test.enigma.console.helper;
//
//import enigma.console.ConsoleCommand;
//import enigma.console.helper.ConsoleValidator;
//
///**
// * Automated test suite for ConsoleValidator utility class.
// *
// * <p>Tests the validation methods used for console input processing:
// * <ul>
// *   <li>Menu command parsing and validation</li>
// *   <li>Positions length validation</li>
// *   <li>Reflector choice validation</li>
// *   <li>Plugboard format validation</li>
// * </ul>
// */
//public class ConsoleValidatorTester {
//
//    public static void main(String[] args) {
//        int passed = 0;
//        int failed = 0;
//
//        System.out.println("========================================");
//        System.out.println(" ConsoleValidator Test Suite");
//        System.out.println("========================================\n");
//
//        // Test parseCommand
//        boolean test1 = testParseCommand();
//        passed += test1 ? 1 : 0;
//        failed += test1 ? 0 : 1;
//
//        // Test parseCommand errors
//        boolean test2 = testParseCommandErrors();
//        passed += test2 ? 1 : 0;
//        failed += test2 ? 0 : 1;
//
//        // Test ensurePositionsLengthMatches
//        boolean test3 = testEnsurePositionsLengthMatches();
//        passed += test3 ? 1 : 0;
//        failed += test3 ? 0 : 1;
//
//        // Test ensureReflectorChoiceInRange
//        boolean test4 = testEnsureReflectorChoiceInRange();
//        passed += test4 ? 1 : 0;
//        failed += test4 ? 0 : 1;
//
//        // Test validatePlugboardFormat
//        boolean test5 = testValidatePlugboardFormat();
//        passed += test5 ? 1 : 0;
//        failed += test5 ? 0 : 1;
//
//        // Summary
//        System.out.println("\n=========== SUMMARY ===========");
//        System.out.println("Total tests : " + (passed + failed));
//        System.out.println("Passed      : " + passed);
//        System.out.println("Failed      : " + failed);
//        if (failed == 0) {
//            System.out.println("Status      : ALL GOOD ✔✔✔");
//        } else {
//            System.out.println("Status      : CHECK FAILED CASES ✘");
//        }
//        System.out.println("================================");
//    }
//
//    private static boolean testParseCommand() {
//        System.out.println("========== Test: parseCommand ==========");
//        boolean allPassed = true;
//
//        // Test case 1: valid command "1"
//        try {
//            ConsoleCommand cmd1 = ConsoleValidator.parseCommand("1");
//            boolean test1 = cmd1 == ConsoleCommand.LOAD_MACHINE_FROM_XML;
//            System.out.println("Input: \"1\" -> Expected: LOAD_MACHINE_FROM_XML, Actual: " + cmd1 + " " + (test1 ? "✔" : "✘"));
//            allPassed &= test1;
//        } catch (Exception e) {
//            System.out.println("Input: \"1\" -> Expected: LOAD_MACHINE_FROM_XML, Actual: exception ✘");
//            allPassed = false;
//        }
//
//        // Test case 2: valid command "8"
//        try {
//            ConsoleCommand cmd2 = ConsoleValidator.parseCommand("8");
//            boolean test2 = cmd2 == ConsoleCommand.EXIT;
//            System.out.println("Input: \"8\" -> Expected: EXIT, Actual: " + cmd2 + " " + (test2 ? "✔" : "✘"));
//            allPassed &= test2;
//        } catch (Exception e) {
//            System.out.println("Input: \"8\" -> Expected: EXIT, Actual: exception ✘");
//            allPassed = false;
//        }
//
//        // Test case 3: valid command with spaces " 3 "
//        try {
//            ConsoleCommand cmd3 = ConsoleValidator.parseCommand(" 3 ");
//            boolean test3 = cmd3 == ConsoleCommand.SET_MANUAL_CODE;
//            System.out.println("Input: \" 3 \" -> Expected: SET_MANUAL_CODE, Actual: " + cmd3 + " " + (test3 ? "✔" : "✘"));
//            allPassed &= test3;
//        } catch (Exception e) {
//            System.out.println("Input: \" 3 \" -> Expected: SET_MANUAL_CODE, Actual: exception ✘");
//            allPassed = false;
//        }
//
//        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
//        System.out.println();
//        return allPassed;
//    }
//
//    private static boolean testParseCommandErrors() {
//        System.out.println("========== Test: parseCommand Error Cases ==========");
//        boolean allPassed = true;
//
//        // Test case 1: non-numeric input "abc"
//        try {
//            ConsoleValidator.parseCommand("abc");
//            System.out.println("Input: \"abc\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"abc\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 2: out of range "0"
//        try {
//            ConsoleValidator.parseCommand("0");
//            System.out.println("Input: \"0\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"0\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 3: out of range "99"
//        try {
//            ConsoleValidator.parseCommand("99");
//            System.out.println("Input: \"99\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"99\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 4: empty input
//        try {
//            ConsoleValidator.parseCommand("");
//            System.out.println("Input: \"\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 5: null input
//        try {
//            ConsoleValidator.parseCommand(null);
//            System.out.println("Input: null -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: null -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
//        System.out.println();
//        return allPassed;
//    }
//
//    private static boolean testEnsurePositionsLengthMatches() {
//        System.out.println("========== Test: ensurePositionsLengthMatches ==========");
//        boolean allPassed = true;
//
//        // Test case 1: matching length
//        try {
//            ConsoleValidator.ensurePositionsLengthMatches("ABC", 3);
//            System.out.println("Input: \"ABC\", rotorCount: 3 -> Expected: no exception, Actual: no exception ✔");
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"ABC\", rotorCount: 3 -> Expected: no exception, Actual: IllegalArgumentException ✘");
//            allPassed = false;
//        }
//
//        // Test case 2: mismatched length
//        try {
//            ConsoleValidator.ensurePositionsLengthMatches("ABC", 4);
//            System.out.println("Input: \"ABC\", rotorCount: 4 -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"ABC\", rotorCount: 4 -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 3: empty string
//        try {
//            ConsoleValidator.ensurePositionsLengthMatches("", 3);
//            System.out.println("Input: \"\", rotorCount: 3 -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"\", rotorCount: 3 -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 4: null string
//        try {
//            ConsoleValidator.ensurePositionsLengthMatches(null, 3);
//            System.out.println("Input: null, rotorCount: 3 -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: null, rotorCount: 3 -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
//        System.out.println();
//        return allPassed;
//    }
//
//    private static boolean testEnsureReflectorChoiceInRange() {
//        System.out.println("========== Test: ensureReflectorChoiceInRange ==========");
//        boolean allPassed = true;
//
//        // Test case 1: valid choice
//        try {
//            ConsoleValidator.ensureReflectorChoiceInRange(2, 5);
//            System.out.println("Input: choice: 2, reflectorsCount: 5 -> Expected: no exception, Actual: no exception ✔");
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: choice: 2, reflectorsCount: 5 -> Expected: no exception, Actual: IllegalArgumentException ✘");
//            allPassed = false;
//        }
//
//        // Test case 2: choice too low
//        try {
//            ConsoleValidator.ensureReflectorChoiceInRange(0, 5);
//            System.out.println("Input: choice: 0, reflectorsCount: 5 -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: choice: 0, reflectorsCount: 5 -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 3: choice too high
//        try {
//            ConsoleValidator.ensureReflectorChoiceInRange(6, 5);
//            System.out.println("Input: choice: 6, reflectorsCount: 5 -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: choice: 6, reflectorsCount: 5 -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
//        System.out.println();
//        return allPassed;
//    }
//
//    private static boolean testValidatePlugboardFormat() {
//        System.out.println("========== Test: validatePlugboardFormat ==========");
//        boolean allPassed = true;
//
//        // Test case 1: valid even-length plugboard
//        try {
//            ConsoleValidator.validatePlugboardFormat("ABCD");
//            System.out.println("Input: \"ABCD\" -> Expected: no exception, Actual: no exception ✔");
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"ABCD\" -> Expected: no exception, Actual: IllegalArgumentException ✘");
//            allPassed = false;
//        }
//
//        // Test case 2: odd-length plugboard
//        try {
//            ConsoleValidator.validatePlugboardFormat("ABC");
//            System.out.println("Input: \"ABC\" -> Expected: IllegalArgumentException, Actual: no exception ✘");
//            allPassed = false;
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"ABC\" -> Expected: IllegalArgumentException, Actual: IllegalArgumentException ✔");
//        }
//
//        // Test case 3: empty plugboard (valid)
//        try {
//            ConsoleValidator.validatePlugboardFormat("");
//            System.out.println("Input: \"\" -> Expected: no exception, Actual: no exception ✔");
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: \"\" -> Expected: no exception, Actual: IllegalArgumentException ✘");
//            allPassed = false;
//        }
//
//        // Test case 4: null plugboard (valid)
//        try {
//            ConsoleValidator.validatePlugboardFormat(null);
//            System.out.println("Input: null -> Expected: no exception, Actual: no exception ✔");
//        } catch (IllegalArgumentException e) {
//            System.out.println("Input: null -> Expected: no exception, Actual: IllegalArgumentException ✘");
//            allPassed = false;
//        }
//
//        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
//        System.out.println();
//        return allPassed;
//    }
//}
