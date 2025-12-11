package test.enigma.engine.snapshot;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.state.MachineState;
import enigma.shared.state.CodeState;

import java.nio.file.Paths;
import java.util.List;

/**
 * Tests complete round-trip snapshot functionality.
 * Verifies that:
 * - Save captures complete state
 * - Load restores exact state
 * - Original vs current positions are preserved
 * - History is preserved
 * - Reset works correctly after load
 */
public class SnapshotRoundTripTester {

    private static final String XML_PATH = Paths.get("enigma-loader/src/test/resources/xml/ex1-sanity-paper-enigma.xml").toString();
    private static final String SNAPSHOT_BASE = "/tmp/snapshot-roundtrip-test";

    public static void main(String[] args) {
        try {
            System.out.println("=== Snapshot Round-Trip Test ===\n");
            
            testScenarioA_NoMessagesProcessed();
            testScenarioB_AfterSingleProcess();
            testScenarioC_ManyProcessOperations();
            testScenarioD_BeforeCodeSelection();
            testResetAfterLoad();
            testMultipleConfigurations();
            
            System.out.println("\n✅ All snapshot round-trip tests PASSED!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Test FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Scenario A: Save/load with no messages processed
     * Verifies basic configuration snapshot
     */
    private static void testScenarioA_NoMessagesProcessed() throws Exception {
        System.out.println("Test A: No messages processed");
        
        Engine engine1 = new EngineImpl();
        engine1.loadMachine(XML_PATH);
        engine1.configManual(new CodeConfig(List.of(1, 2, 3), List.of('O', 'D', 'X'), "I"));
        
        MachineState before = engine1.machineData();
        engine1.saveSnapshot(SNAPSHOT_BASE + "-a");
        
        Engine engine2 = new EngineImpl();
        engine2.loadSnapshot(SNAPSHOT_BASE + "-a");
        MachineState after = engine2.machineData();
        
        assertState("A", before, after);
        assertHistory("A", engine1.history(), engine2.history());
        
        System.out.println("  ✓ State preserved: " + after.ogCodeState());
        System.out.println();
    }

    /**
     * Scenario B: Save/load after single process operation
     * Verifies stepped rotor positions are captured
     */
    private static void testScenarioB_AfterSingleProcess() throws Exception {
        System.out.println("Test B: After single process");
        
        Engine engine1 = new EngineImpl();
        engine1.loadMachine(XML_PATH);
        engine1.configManual(new CodeConfig(List.of(1, 2, 3), List.of('O', 'D', 'X'), "I"));
        engine1.process("A");
        
        MachineState before = engine1.machineData();
        String historyBefore = engine1.history();
        
        // Verify positions changed
        if (before.ogCodeState().positions().equals(before.curCodeState().positions())) {
            throw new AssertionError("Positions should differ after processing");
        }
        
        engine1.saveSnapshot(SNAPSHOT_BASE + "-b");
        
        Engine engine2 = new EngineImpl();
        engine2.loadSnapshot(SNAPSHOT_BASE + "-b");
        MachineState after = engine2.machineData();
        String historyAfter = engine2.history();
        
        assertState("B", before, after);
        assertHistory("B", historyBefore, historyAfter);
        
        System.out.println("  ✓ Original: " + after.ogCodeState());
        System.out.println("  ✓ Current:  " + after.curCodeState());
        System.out.println("  ✓ Positions differ as expected");
        System.out.println();
    }

    /**
     * Scenario C: Save/load after many process operations
     * Verifies large history sets are consistent
     */
    private static void testScenarioC_ManyProcessOperations() throws Exception {
        System.out.println("Test C: Many process operations");
        
        Engine engine1 = new EngineImpl();
        engine1.loadMachine(XML_PATH);
        engine1.configManual(new CodeConfig(List.of(1, 2, 3), List.of('O', 'D', 'X'), "I"));
        
        // Process multiple messages
        engine1.process("HELLO");
        engine1.process("WORLD");
        engine1.process("TEST");
        engine1.process("SNAPSHOT");
        
        MachineState before = engine1.machineData();
        String historyBefore = engine1.history();
        
        engine1.saveSnapshot(SNAPSHOT_BASE + "-c");
        
        Engine engine2 = new EngineImpl();
        engine2.loadSnapshot(SNAPSHOT_BASE + "-c");
        MachineState after = engine2.machineData();
        String historyAfter = engine2.history();
        
        assertState("C", before, after);
        assertHistory("C", historyBefore, historyAfter);
        
        // Verify can continue processing
        engine2.process("CONTINUE");
        
        System.out.println("  ✓ Processed: " + after.stringsProcessed() + " messages");
        System.out.println("  ✓ Can continue processing after load");
        System.out.println();
    }

    /**
     * Scenario D: Save/load before code selection
     * Verifies snapshot without configuration doesn't error
     */
    private static void testScenarioD_BeforeCodeSelection() throws Exception {
        System.out.println("Test D: Before code selection");
        
        Engine engine1 = new EngineImpl();
        engine1.loadMachine(XML_PATH);
        
        engine1.saveSnapshot(SNAPSHOT_BASE + "-d");
        
        Engine engine2 = new EngineImpl();
        engine2.loadSnapshot(SNAPSHOT_BASE + "-d");
        MachineState after = engine2.machineData();
        
        if (after.stringsProcessed() != 0) {
            throw new AssertionError("Strings processed should be 0");
        }
        
        // Should be able to configure after load
        engine2.configManual(new CodeConfig(List.of(1, 2, 3), List.of('A', 'A', 'A'), "I"));
        engine2.process("TEST");
        
        System.out.println("  ✓ Loaded without configuration");
        System.out.println("  ✓ Can configure and process after load");
        System.out.println();
    }

    /**
     * Test reset functionality after loading snapshot
     */
    private static void testResetAfterLoad() throws Exception {
        System.out.println("Test: Reset after load");
        
        Engine engine1 = new EngineImpl();
        engine1.loadMachine(XML_PATH);
        engine1.configManual(new CodeConfig(List.of(1, 2, 3), List.of('O', 'D', 'X'), "I"));
        engine1.process("HELLO");
        engine1.reset();
        engine1.process("WORLD");
        
        MachineState before = engine1.machineData();
        engine1.saveSnapshot(SNAPSHOT_BASE + "-reset");
        
        Engine engine2 = new EngineImpl();
        engine2.loadSnapshot(SNAPSHOT_BASE + "-reset");
        
        // Test reset on loaded engine
        engine2.reset();
        MachineState afterReset = engine2.machineData();
        
        // Positions should match original after reset
        if (!afterReset.ogCodeState().positions().equals(afterReset.curCodeState().positions())) {
            throw new AssertionError("Reset should restore original positions. " +
                "Original: " + afterReset.ogCodeState().positions() + 
                ", Current: " + afterReset.curCodeState().positions());
        }
        
        System.out.println("  ✓ Reset works correctly after load");
        System.out.println("  ✓ Original: " + afterReset.ogCodeState());
        System.out.println("  ✓ Current:  " + afterReset.curCodeState());
        System.out.println();
    }

    /**
     * Test snapshot with multiple configurations in history
     */
    private static void testMultipleConfigurations() throws Exception {
        System.out.println("Test: Multiple configurations");
        
        Engine engine1 = new EngineImpl();
        engine1.loadMachine(XML_PATH);
        
        // First configuration
        engine1.configManual(new CodeConfig(List.of(1, 2, 3), List.of('O', 'D', 'X'), "I"));
        engine1.process("FIRST");
        
        // Second configuration
        engine1.configManual(new CodeConfig(List.of(3, 2, 1), List.of('A', 'B', 'C'), "I"));
        engine1.process("SECOND");
        
        String historyBefore = engine1.history();
        engine1.saveSnapshot(SNAPSHOT_BASE + "-multi");
        
        Engine engine2 = new EngineImpl();
        engine2.loadSnapshot(SNAPSHOT_BASE + "-multi");
        String historyAfter = engine2.history();
        
        assertHistory("Multi", historyBefore, historyAfter);
        
        // Verify both configurations are in history
        if (!historyAfter.contains("FIRST") || !historyAfter.contains("SECOND")) {
            throw new AssertionError("History should contain both messages");
        }
        
        System.out.println("  ✓ Multiple configurations preserved");
        System.out.println("  ✓ Both messages in history");
        System.out.println();
    }

    // Helper assertion methods
    
    private static void assertState(String scenario, MachineState expected, MachineState actual) {
        if (expected.stringsProcessed() != actual.stringsProcessed()) {
            throw new AssertionError(scenario + ": Strings processed mismatch. " +
                "Expected: " + expected.stringsProcessed() + ", Actual: " + actual.stringsProcessed());
        }
        
        if (!equalsOrBothNull(expected.ogCodeState(), actual.ogCodeState())) {
            throw new AssertionError(scenario + ": Original code state mismatch. " +
                "Expected: " + expected.ogCodeState() + ", Actual: " + actual.ogCodeState());
        }
        
        if (!equalsOrBothNull(expected.curCodeState(), actual.curCodeState())) {
            throw new AssertionError(scenario + ": Current code state mismatch. " +
                "Expected: " + expected.curCodeState() + ", Actual: " + actual.curCodeState());
        }
    }
    
    private static void assertHistory(String scenario, String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(scenario + ": History mismatch.\n" +
                "Expected:\n" + expected + "\n\nActual:\n" + actual);
        }
    }
    
    private static boolean equalsOrBothNull(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
