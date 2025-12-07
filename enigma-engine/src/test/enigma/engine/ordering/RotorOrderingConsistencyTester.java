package test.enigma.engine.ordering;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.state.CodeState;

import java.nio.file.Paths;
import java.util.List;

/**
 * Rotor Ordering Consistency Tester
 * 
 * <p>This tester verifies the consistency of rotor ordering logic throughout
 * the Enigma machine implementation. It ensures that:</p>
 * <ul>
 *   <li>Input rotor order is left→right (user perspective)</li>
 *   <li>Internal rotor order is preserved as left→right with correct mapping</li>
 *   <li>Position strings match the left→right convention</li>
 *   <li>Stepping logic correctly identifies rightmost rotor for stepping</li>
 * </ul>
 * 
 * <h2>Test Case: Configuration &lt;3,2,1&gt; with positions "ABC"</h2>
 * <p>When configured with rotor IDs [3, 2, 1] in left→right order and
 * positions "ABC", the internal mapping should be:</p>
 * <ul>
 *   <li>Index 0 (leftmost): Rotor 3 at position 'A'</li>
 *   <li>Index 1 (middle): Rotor 2 at position 'B'</li>
 *   <li>Index 2 (rightmost): Rotor 1 at position 'C'</li>
 * </ul>
 * 
 * <p>The stepping logic should always step the rightmost rotor first
 * (index 2 in the internal array, which is Rotor 1).</p>
 * 
 * <p><b>Note:</b> This is a manual test runner designed to be executed
 * directly with {@code java} command. It follows the same pattern as other
 * manual test runners in the project (e.g., PaperSingleCharTester).</p>
 * 
 * @since 1.0
 */
public class RotorOrderingConsistencyTester {

    // Use the same test resources directory as other test runners
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";
    private static final String XML_PATH = Paths.get(XML_BASE_DIR, "ex1-sanity-paper-enigma.xml").toString();

    public static void main(String[] args) {
        boolean success = runAllTests();
        if (!success) {
            // Exit with error code for manual test runners
            System.exit(1);
        }
    }

    /**
     * Run all rotor ordering consistency tests.
     * 
     * @return true if all tests passed, false otherwise
     */
    private static boolean runAllTests() {
        System.out.println("=".repeat(70));
        System.out.println("ROTOR ORDERING CONSISTENCY TEST");
        System.out.println("=".repeat(70));
        System.out.println();

        Engine engine = new EngineImpl();

        // Load machine specification
        System.out.println("Step 1: Loading machine specification");
        System.out.println("  XML: " + XML_PATH);
        try {
            engine.loadMachine(XML_PATH);
            System.out.println("  ✓ Machine loaded successfully");
        } catch (Exception e) {
            System.err.println("  ✗ Failed to load machine: " + e.getMessage());
            System.err.println("  Note: This test requires XML test files to be present.");
            return false;
        }
        System.out.println();

        // Test Case 1: Configuration <3,2,1><ABC><I>
        boolean test1 = testConfiguration321(engine);
        if (!test1) {
            return false;
        }

        // Test Case 2: Configuration <1,2,3><ODX><I>
        boolean test2 = testConfiguration123(engine);
        if (!test2) {
            return false;
        }

        // Final summary
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("✓ ALL TESTS PASSED");
        System.out.println();
        System.out.println("Rotor ordering consistency verified:");
        System.out.println("  • Input rotor order is left→right (user perspective)");
        System.out.println("  • Internal rotor order preserves left→right mapping");
        System.out.println("  • Position strings match left→right convention");
        System.out.println("  • Stepping logic correctly uses rightmost rotor (highest index)");
        System.out.println();

        return true;
    }

    /**
     * Test configuration &lt;3,2,1&gt;&lt;ABC&gt;&lt;I&gt;.
     * 
     * @param engine the engine instance to test with
     * @return true if test passed, false otherwise
     */
    private static boolean testConfiguration321(Engine engine) {
        System.out.println("Step 2: Testing configuration <3,2,1><ABC><I>");
        System.out.println("  Expected behavior:");
        System.out.println("    - Rotor IDs in left→right order: [3, 2, 1]");
        System.out.println("    - Positions in left→right order: [A, B, C]");
        System.out.println("    - Internal mapping:");
        System.out.println("        Index 0 (leftmost):  Rotor 3 at position 'A'");
        System.out.println("        Index 1 (middle):    Rotor 2 at position 'B'");
        System.out.println("        Index 2 (rightmost): Rotor 1 at position 'C'");
        System.out.println();

        CodeConfig config321 = new CodeConfig(
            List.of(3, 2, 1),           // rotors left→right
            List.of('A', 'B', 'C'),     // positions left→right
            "I"                          // reflector
        );

        try {
            engine.configManual(config321);
            System.out.println("  ✓ Configuration applied successfully");
        } catch (Exception e) {
            System.err.println("  ✗ Failed to configure: " + e.getMessage());
            return false;
        }

        // Verify the configuration was stored correctly
        CodeConfig currentConfig = engine.getCurrentCodeConfig();
        System.out.println();
        System.out.println("Step 3: Verifying stored configuration");
        System.out.println("  Rotor IDs: " + currentConfig.rotorIds());
        System.out.println("  Positions: " + currentConfig.positions());
        System.out.println("  Reflector: " + currentConfig.reflectorId());

        boolean rotorIdsMatch = currentConfig.rotorIds().equals(List.of(3, 2, 1));
        boolean positionsMatch = currentConfig.positions().equals(List.of('A', 'B', 'C'));
        boolean reflectorMatches = "I".equals(currentConfig.reflectorId());

        System.out.println();
        System.out.println("  Rotor IDs match expected: " + (rotorIdsMatch ? "✓" : "✗"));
        System.out.println("  Positions match expected: " + (positionsMatch ? "✓" : "✗"));
        System.out.println("  Reflector matches expected: " + (reflectorMatches ? "✓" : "✗"));

        if (!rotorIdsMatch || !positionsMatch || !reflectorMatches) {
            System.err.println();
            System.err.println("  ✗ Configuration mismatch detected!");
            return false;
        }

        // Test stepping logic
        System.out.println();
        System.out.println("Step 4: Testing stepping logic");
        System.out.println("  Processing single character to observe rotor advancement");
        System.out.println("  Expected: Rightmost rotor (Rotor 1 at index 2) advances first");
        System.out.println();

        // Get initial state
        CodeState initialState = engine.machineData().currentCodeState();
        String initialPositions = initialState.positions();
        System.out.println("  Initial positions (left→right): " + initialPositions);
        
        // Process a single character
        String testInput = "A";
        ProcessTrace trace = engine.process(testInput);
        SignalTrace signalTrace = trace.traces().get(0);
        
        String positionsAfter = signalTrace.windowAfter();
        System.out.println("  Positions after 'A' (left→right): " + positionsAfter);
        
        // Verify that the rightmost position changed
        char initialRightmost = initialPositions.charAt(initialPositions.length() - 1);
        char afterRightmost = positionsAfter.charAt(positionsAfter.length() - 1);
        
        boolean rightmostAdvanced = initialRightmost != afterRightmost;
        System.out.println();
        System.out.println("  Rightmost rotor advanced: " + (rightmostAdvanced ? "✓" : "✗"));
        System.out.println("    Before: '" + initialRightmost + "' -> After: '" + afterRightmost + "'");

        // Check which rotors advanced according to the trace
        List<Integer> advancedIndices = signalTrace.advancedIndices();
        System.out.println();
        System.out.println("  Advanced rotor indices: " + advancedIndices);
        System.out.println("  Note: Indices are in left→right format (0=leftmost, 2=rightmost)");
        
        // The rightmost rotor should always be in the advanced list
        boolean rightmostInAdvanced = advancedIndices.contains(2);
        System.out.println("  Rightmost rotor (index 2) in advanced list: " + (rightmostInAdvanced ? "✓" : "✗"));

        return rightmostAdvanced && rightmostInAdvanced;
    }

    /**
     * Test configuration &lt;1,2,3&gt;&lt;ODX&gt;&lt;I&gt;.
     * 
     * @param engine the engine instance to test with
     * @return true if test passed, false otherwise
     */
    private static boolean testConfiguration123(Engine engine) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("Step 5: Testing alternative configuration <1,2,3><ODX><I>");
        System.out.println("  Expected behavior:");
        System.out.println("    - Rotor IDs in left→right order: [1, 2, 3]");
        System.out.println("    - Positions in left→right order: [O, D, X]");
        System.out.println("    - Internal mapping:");
        System.out.println("        Index 0 (leftmost):  Rotor 1 at position 'O'");
        System.out.println("        Index 1 (middle):    Rotor 2 at position 'D'");
        System.out.println("        Index 2 (rightmost): Rotor 3 at position 'X'");
        System.out.println();

        CodeConfig config123 = new CodeConfig(
            List.of(1, 2, 3),           // rotors left→right
            List.of('O', 'D', 'X'),     // positions left→right
            "I"                          // reflector
        );

        try {
            engine.configManual(config123);
            System.out.println("  ✓ Configuration applied successfully");
        } catch (Exception e) {
            System.err.println("  ✗ Failed to configure: " + e.getMessage());
            return false;
        }

        // Verify the second configuration
        CodeConfig currentConfig2 = engine.getCurrentCodeConfig();
        System.out.println();
        System.out.println("  Rotor IDs: " + currentConfig2.rotorIds());
        System.out.println("  Positions: " + currentConfig2.positions());
        
        boolean rotorIdsMatch2 = currentConfig2.rotorIds().equals(List.of(1, 2, 3));
        boolean positionsMatch2 = currentConfig2.positions().equals(List.of('O', 'D', 'X'));
        
        System.out.println();
        System.out.println("  Rotor IDs match expected: " + (rotorIdsMatch2 ? "✓" : "✗"));
        System.out.println("  Positions match expected: " + (positionsMatch2 ? "✓" : "✗"));

        // Test encryption with this configuration
        System.out.println();
        System.out.println("  Testing encryption with known value");
        String input2 = "T";
        String expected2 = "A"; // Known value from PaperSingleCharTester
        ProcessTrace trace2 = engine.process(input2);
        String actual2 = trace2.output();
        
        System.out.println("    Input:    '" + input2 + "'");
        System.out.println("    Expected: '" + expected2 + "'");
        System.out.println("    Actual:   '" + actual2 + "'");
        boolean encryptionMatches = expected2.equals(actual2);
        System.out.println("    Match: " + (encryptionMatches ? "✓" : "✗"));

        return rotorIdsMatch2 && positionsMatch2 && encryptionMatches;
    }
}
