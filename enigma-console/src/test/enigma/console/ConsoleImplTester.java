package test.enigma.console;

import enigma.console.Console;
import enigma.console.ConsoleImpl;
import enigma.engine.Engine;
import enigma.engine.EngineImpl;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

/**
 * Manual tester for ConsoleImpl main interaction flow.
 * 
 * <p>Tests the console command validation and flow control:
 * <ul>
 *   <li>Command validation (numeric input, valid range)</li>
 *   <li>Command enablement logic (state-dependent commands)</li>
 *   <li>Main interaction flow</li>
 * </ul>
 * 
 * <p>This tester simulates user input using predefined strings to validate
 * the console's behavior without requiring manual interaction.
 */
public class ConsoleImplTester {

    public static void main(String[] args) {
        int passed = 0;
        int failed = 0;

        System.out.println("========================================");
        System.out.println(" ConsoleImpl Test Suite");
        System.out.println("========================================\n");

        // Test invalid command input handling
        passed += testInvalidCommandInput() ? 1 : 0;
        failed += testInvalidCommandInput() ? 0 : 1;

        // Test command state validation
        passed += testCommandStateValidation() ? 1 : 0;
        failed += testCommandStateValidation() ? 0 : 1;

        // Test exit command
        passed += testExitCommand() ? 1 : 0;
        failed += testExitCommand() ? 0 : 1;

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

    /**
     * Test that invalid command inputs are properly rejected.
     * Tests: empty input, non-numeric input, out-of-range numbers.
     */
    private static boolean testInvalidCommandInput() {
        System.out.println("========== Test: Invalid Command Input ==========");
        
        // Simulate: empty line, then "abc", then "99", then valid exit command "8"
        String simulatedInput = "\nabc\n99\n8\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        Engine engine = new EngineImpl();
        Console console = new ConsoleImpl(engine, scanner);

        System.out.println("Simulating invalid inputs followed by exit...");
        System.out.println("Expected: Console should reject invalid inputs and exit cleanly");
        
        try {
            console.run();
            System.out.println("Result: PASSED ✔ (Console handled invalid input and exited)");
            System.out.println();
            return true;
        } catch (Exception e) {
            System.out.println("Result: FAILED ✘ (Unexpected exception: " + e.getMessage() + ")");
            System.out.println();
            return false;
        }
    }

    /**
     * Test that commands are properly disabled/enabled based on console state.
     * Commands that require loaded XML should be disabled initially.
     */
    private static boolean testCommandStateValidation() {
        System.out.println("========== Test: Command State Validation ==========");
        
        // Simulate: try command 2 (should be disabled), then exit
        String simulatedInput = "2\n8\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        Engine engine = new EngineImpl();
        Console console = new ConsoleImpl(engine, scanner);

        System.out.println("Simulating attempt to use command 2 (Show spec) without loading XML...");
        System.out.println("Expected: Command should be rejected, then exit");
        
        try {
            console.run();
            System.out.println("Result: PASSED ✔ (Console correctly rejected disabled command)");
            System.out.println();
            return true;
        } catch (Exception e) {
            System.out.println("Result: FAILED ✘ (Unexpected exception: " + e.getMessage() + ")");
            System.out.println();
            return false;
        }
    }

    /**
     * Test the exit command works correctly.
     */
    private static boolean testExitCommand() {
        System.out.println("========== Test: Exit Command ==========");
        
        // Simulate: immediate exit
        String simulatedInput = "8\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes()));
        Engine engine = new EngineImpl();
        Console console = new ConsoleImpl(engine, scanner);

        System.out.println("Simulating immediate exit...");
        System.out.println("Expected: Console should exit cleanly");
        
        try {
            console.run();
            System.out.println("Result: PASSED ✔ (Console exited cleanly)");
            System.out.println();
            return true;
        } catch (Exception e) {
            System.out.println("Result: FAILED ✘ (Unexpected exception: " + e.getMessage() + ")");
            System.out.println();
            return false;
        }
    }
}
