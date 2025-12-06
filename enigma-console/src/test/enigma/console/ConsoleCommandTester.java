package test.enigma.console;

import enigma.console.ConsoleCommand;

/**
 * Manual tester for ConsoleCommand enum.
 * 
 * <p>Tests command parsing and validation:
 * <ul>
 *   <li>Command ID lookup (fromId method)</li>
 *   <li>Command properties (ID and description)</li>
 *   <li>Invalid command handling</li>
 * </ul>
 */
public class ConsoleCommandTester {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("========================================");
        System.out.println(" ConsoleCommand Test Suite");
        System.out.println("========================================\n");

        // Test fromId with valid IDs
        boolean test1 = testFromIdValid();
        passed += test1 ? 1 : 0;
        failed += test1 ? 0 : 1;

        // Test fromId with invalid IDs
        boolean test2 = testFromIdInvalid();
        passed += test2 ? 1 : 0;
        failed += test2 ? 0 : 1;

        // Test all commands have correct properties
        boolean test3 = testCommandProperties();
        passed += test3 ? 1 : 0;
        failed += test3 ? 0 : 1;

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

    private static boolean testFromIdValid() {
        System.out.println("========== Test: fromId with valid IDs ==========");
        boolean allPassed = true;

        // Test all valid command IDs
        ConsoleCommand cmd1 = ConsoleCommand.fromId(1);
        boolean test1 = cmd1 == ConsoleCommand.LOAD_MACHINE_FROM_XML;
        System.out.println("fromId(1) -> Expected: LOAD_MACHINE_FROM_XML, Actual: " + cmd1 + " " + (test1 ? "✔" : "✘"));
        allPassed &= test1;

        ConsoleCommand cmd2 = ConsoleCommand.fromId(2);
        boolean test2 = cmd2 == ConsoleCommand.SHOW_MACHINE_SPEC;
        System.out.println("fromId(2) -> Expected: SHOW_MACHINE_SPEC, Actual: " + cmd2 + " " + (test2 ? "✔" : "✘"));
        allPassed &= test2;

        ConsoleCommand cmd3 = ConsoleCommand.fromId(3);
        boolean test3 = cmd3 == ConsoleCommand.SET_MANUAL_CODE;
        System.out.println("fromId(3) -> Expected: SET_MANUAL_CODE, Actual: " + cmd3 + " " + (test3 ? "✔" : "✘"));
        allPassed &= test3;

        ConsoleCommand cmd4 = ConsoleCommand.fromId(4);
        boolean test4 = cmd4 == ConsoleCommand.SET_AUTOMATIC_CODE;
        System.out.println("fromId(4) -> Expected: SET_AUTOMATIC_CODE, Actual: " + cmd4 + " " + (test4 ? "✔" : "✘"));
        allPassed &= test4;

        ConsoleCommand cmd5 = ConsoleCommand.fromId(5);
        boolean test5 = cmd5 == ConsoleCommand.PROCESS_INPUT;
        System.out.println("fromId(5) -> Expected: PROCESS_INPUT, Actual: " + cmd5 + " " + (test5 ? "✔" : "✘"));
        allPassed &= test5;

        ConsoleCommand cmd6 = ConsoleCommand.fromId(6);
        boolean test6 = cmd6 == ConsoleCommand.RESET_CODE;
        System.out.println("fromId(6) -> Expected: RESET_CODE, Actual: " + cmd6 + " " + (test6 ? "✔" : "✘"));
        allPassed &= test6;

        ConsoleCommand cmd7 = ConsoleCommand.fromId(7);
        boolean test7 = cmd7 == ConsoleCommand.SHOW_HISTORY_AND_STATS;
        System.out.println("fromId(7) -> Expected: SHOW_HISTORY_AND_STATS, Actual: " + cmd7 + " " + (test7 ? "✔" : "✘"));
        allPassed &= test7;

        ConsoleCommand cmd8 = ConsoleCommand.fromId(8);
        boolean test8 = cmd8 == ConsoleCommand.EXIT;
        System.out.println("fromId(8) -> Expected: EXIT, Actual: " + cmd8 + " " + (test8 ? "✔" : "✘"));
        allPassed &= test8;

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }

    private static boolean testFromIdInvalid() {
        System.out.println("========== Test: fromId with invalid IDs ==========");
        boolean allPassed = true;

        // Test invalid IDs (should return null)
        ConsoleCommand cmd0 = ConsoleCommand.fromId(0);
        boolean test0 = cmd0 == null;
        System.out.println("fromId(0) -> Expected: null, Actual: " + cmd0 + " " + (test0 ? "✔" : "✘"));
        allPassed &= test0;

        ConsoleCommand cmd9 = ConsoleCommand.fromId(9);
        boolean test9 = cmd9 == null;
        System.out.println("fromId(9) -> Expected: null, Actual: " + cmd9 + " " + (test9 ? "✔" : "✘"));
        allPassed &= test9;

        ConsoleCommand cmdNeg = ConsoleCommand.fromId(-1);
        boolean testNeg = cmdNeg == null;
        System.out.println("fromId(-1) -> Expected: null, Actual: " + cmdNeg + " " + (testNeg ? "✔" : "✘"));
        allPassed &= testNeg;

        ConsoleCommand cmd100 = ConsoleCommand.fromId(100);
        boolean test100 = cmd100 == null;
        System.out.println("fromId(100) -> Expected: null, Actual: " + cmd100 + " " + (test100 ? "✔" : "✘"));
        allPassed &= test100;

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }

    private static boolean testCommandProperties() {
        System.out.println("========== Test: Command Properties ==========");
        boolean allPassed = true;

        // Verify each command has the expected ID and a non-empty description
        for (ConsoleCommand cmd : ConsoleCommand.values()) {
            boolean hasValidId = cmd.getId() >= 1 && cmd.getId() <= 8;
            boolean hasDescription = cmd.getDescription() != null && !cmd.getDescription().isEmpty();
            boolean commandOk = hasValidId && hasDescription;
            
            System.out.println(cmd.name() + " -> ID: " + cmd.getId() + 
                             ", Description: \"" + cmd.getDescription() + "\" " + 
                             (commandOk ? "✔" : "✘"));
            allPassed &= commandOk;
        }

        // Verify uniqueness of IDs
        boolean uniqueIds = true;
        for (int i = 1; i <= 8; i++) {
            int count = 0;
            for (ConsoleCommand cmd : ConsoleCommand.values()) {
                if (cmd.getId() == i) {
                    count++;
                }
            }
            if (count != 1) {
                System.out.println("ID " + i + " appears " + count + " times (expected 1) ✘");
                uniqueIds = false;
                allPassed = false;
            }
        }
        if (uniqueIds) {
            System.out.println("All command IDs are unique ✔");
        }

        System.out.println("Result: " + (allPassed ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();
        return allPassed;
    }
}
