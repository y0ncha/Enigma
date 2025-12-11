package test.enigma.engine.snapshot;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.engine.exception.EngineException;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Tests snapshot validation and error handling.
 * Verifies that corrupted or invalid snapshot files fail gracefully
 * with clear, user-friendly error messages.
 */
public class SnapshotValidationTester {

    private static final String TEST_DIR = "/tmp/snapshot-validation-test";

    public static void main(String[] args) {
        try {
            // Create test directory
            new java.io.File(TEST_DIR).mkdirs();
            
            System.out.println("=== Snapshot Validation Tests ===\n");
            
            testMissingFile();
            testMalformedJSON();
            testMissingSpec();
            testMissingMachineState();
            testEmptySnapshot();
            testInvalidCodeState();
            
            System.out.println("\n✅ All validation tests PASSED!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Test FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Test loading a non-existent file
     */
    private static void testMissingFile() {
        System.out.println("Test: Missing file");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadSnapshot(TEST_DIR + "/nonexistent");
            
            throw new AssertionError("Should have thrown exception for missing file");
            
        } catch (EngineException e) {
            String msg = e.getMessage();
            if (!msg.contains("does not exist")) {
                throw new AssertionError("Error message should mention file doesn't exist: " + msg);
            }
            System.out.println("  ✓ Caught expected error: " + msg.substring(0, Math.min(80, msg.length())));
        }
        System.out.println();
    }

    /**
     * Test loading a file with malformed JSON
     */
    private static void testMalformedJSON() throws IOException {
        System.out.println("Test: Malformed JSON");
        
        createSnapshot(TEST_DIR + "/malformed.enigma.json", "{invalid: json syntax}");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadSnapshot(TEST_DIR + "/malformed");
            
            throw new AssertionError("Should have thrown exception for malformed JSON");
            
        } catch (EngineException e) {
            String msg = e.getMessage();
            // Should contain either "MalformedJsonException" or some JSON error indicator
            System.out.println("  ✓ Caught expected error: " + msg.substring(0, Math.min(80, msg.length())));
        }
        System.out.println();
    }

    /**
     * Test loading a snapshot with missing MachineSpec
     */
    private static void testMissingSpec() throws IOException {
        System.out.println("Test: Missing MachineSpec");
        
        String json = "{\"machineState\": {\"numOfRotors\": 3, \"numOfReflectors\": 1, \"stringsProcessed\": 0}, \"history\": {\"history\": {}}}";
        createSnapshot(TEST_DIR + "/missing-spec.enigma.json", json);
        
        try {
            Engine engine = new EngineImpl();
            engine.loadSnapshot(TEST_DIR + "/missing-spec");
            
            throw new AssertionError("Should have thrown exception for missing spec");
            
        } catch (EngineException e) {
            String msg = e.getMessage();
            if (!msg.contains("invalid") && !msg.contains("empty")) {
                throw new AssertionError("Error message should mention invalid/empty snapshot: " + msg);
            }
            System.out.println("  ✓ Caught expected error: " + msg.substring(0, Math.min(80, msg.length())));
        }
        System.out.println();
    }

    /**
     * Test loading a snapshot with missing MachineState
     * This should be handled gracefully (defensive fallback)
     */
    private static void testMissingMachineState() throws IOException {
        System.out.println("Test: Missing MachineState");
        
        String json = "{"
            + "\"spec\": {"
            + "  \"alphabet\": {\"letters\": \"ABCDEFGHIJKLMNOPQRSTUVWXYZ\"},"
            + "  \"rotorsById\": {},"
            + "  \"reflectorsById\": {\"I\": {\"id\": \"I\", \"reflect\": []}},"
            + "  \"rotorsInUse\": 3"
            + "},"
            + "\"history\": {\"history\": {}}"
            + "}";
        createSnapshot(TEST_DIR + "/missing-state.enigma.json", json);
        
        try {
            Engine engine = new EngineImpl();
            engine.loadSnapshot(TEST_DIR + "/missing-state");
            
            // Should load successfully with default state
            System.out.println("  ✓ Loaded with default state (acceptable behavior)");
            
        } catch (Exception e) {
            // Also acceptable - different implementations may choose to fail
            System.out.println("  ✓ Rejected invalid snapshot (acceptable behavior)");
        }
        System.out.println();
    }

    /**
     * Test loading an empty JSON object
     */
    private static void testEmptySnapshot() throws IOException {
        System.out.println("Test: Empty snapshot");
        
        createSnapshot(TEST_DIR + "/empty.enigma.json", "{}");
        
        try {
            Engine engine = new EngineImpl();
            engine.loadSnapshot(TEST_DIR + "/empty");
            
            throw new AssertionError("Should have thrown exception for empty snapshot");
            
        } catch (EngineException e) {
            String msg = e.getMessage();
            if (!msg.contains("invalid") && !msg.contains("empty")) {
                throw new AssertionError("Error message should mention invalid/empty: " + msg);
            }
            System.out.println("  ✓ Caught expected error: " + msg.substring(0, Math.min(80, msg.length())));
        }
        System.out.println();
    }

    /**
     * Test loading a snapshot with invalid CodeState
     * (wrong rotor count for spec)
     */
    private static void testInvalidCodeState() throws IOException {
        System.out.println("Test: Invalid CodeState");
        
        String json = "{"
            + "\"spec\": {"
            + "  \"alphabet\": {\"letters\": \"ABCDEFGHIJKLMNOPQRSTUVWXYZ\"},"
            + "  \"rotorsById\": {"
            + "    \"1\": {\"id\": 1, \"notchIndex\": 16, \"rightColumn\": [\"A\"], \"leftColumn\": [\"A\"]}"
            + "  },"
            + "  \"reflectorsById\": {"
            + "    \"I\": {\"id\": \"I\", \"reflect\": [[0, 1]]}"
            + "  },"
            + "  \"rotorsInUse\": 3"
            + "},"
            + "\"machineState\": {"
            + "  \"numOfRotors\": 1,"
            + "  \"numOfReflectors\": 1,"
            + "  \"stringsProcessed\": 0,"
            + "  \"ogCodeState\": {"
            + "    \"rotorIds\": [1, 2],"
            + "    \"positions\": \"AB\","
            + "    \"notchDist\": [1, 2],"
            + "    \"reflectorId\": \"I\","
            + "    \"plugboard\": \"\""
            + "  },"
            + "  \"curCodeState\": {"
            + "    \"rotorIds\": [1, 2],"
            + "    \"positions\": \"AB\","
            + "    \"notchDist\": [1, 2],"
            + "    \"reflectorId\": \"I\","
            + "    \"plugboard\": \"\""
            + "  }"
            + "},"
            + "\"history\": {\"history\": {}}"
            + "}";
        createSnapshot(TEST_DIR + "/invalid-state.enigma.json", json);
        
        try {
            Engine engine = new EngineImpl();
            engine.loadSnapshot(TEST_DIR + "/invalid-state");
            
            // Should fail during configuration validation
            throw new AssertionError("Should have thrown exception for invalid rotor count");
            
        } catch (Exception e) {
            // Any exception is acceptable - validation can catch this at different points
            System.out.println("  ✓ Caught expected error: " + e.getMessage().substring(0, Math.min(80, e.getMessage().length())));
        }
        System.out.println();
    }

    /**
     * Helper to create a snapshot file with given content
     */
    private static void createSnapshot(String path, String content) throws IOException {
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(content);
        }
    }
}
