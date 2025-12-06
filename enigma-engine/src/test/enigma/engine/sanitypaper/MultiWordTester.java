package test.enigma.engine.sanitypaper;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.DebugTrace;

import java.nio.file.Paths;

/**
 * Manual sanity test for the Enigma engine using the "sanity-paper" dataset.
 * Runs the set of inputs from the paper appendix and prints results to stdout.
 * This mirrors the existing SanityPaperTester but kept as a focused multi-case
 * runner for the large single vector case.
 */
public class MultiWordTester {

    // Use the same test resources directory as the loader tests
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";

    private static final String XML_PATH = Paths.get(XML_BASE_DIR, "ex1-sanity-paper-enigma.xml").toString();

    /**
     * Entry point for the sanity test.
     */
    public static void main(String[] args) {

        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH + "\n");

        engine.loadMachine(XML_PATH);

        // Code: <1,2,3><ODX><I>
        CodeConfig config = new CodeConfig(
                java.util.List.of(1, 2, 3),   // rotors left→right
                java.util.List.of(14, 3, 23), // "ODX" (O=14, D=3, X=23)
                "I"                           // reflector
        );

        System.out.println("Code configuration: " + config + "\n");
        engine.configManual(config);

        // Sanity-paper inputs & expected outputs from the paper appendix
        String[] inputs = {
                "WOWCANTBELIEVEITACTUALLYWORKS"
        };

        String[] expectedOutputs = {
                "CVRDIZWDAWQKUKBVHJILPKRNDXWIY"
        };

        int passed = 0;
        int failed = 0;

        for (int i = 0; i < inputs.length; i++) {

            // Re-apply the code before each test so the rotors start at ODX for every case
            engine.configManual(config);

            String input = inputs[i];
            DebugTrace debug = engine.process(input);

            boolean ok = debug.output().equals(expectedOutputs[i]);
            if (ok) passed++; else failed++;

            System.out.println("========== Sanity Case #" + (i + 1) + " ==========");
            System.out.println("Input   : " + input);
            System.out.println("Expected: " + expectedOutputs[i]);
            System.out.println("Actual  : " + debug.output());
            System.out.println("Result  : " + (ok ? "PASSED ✔" : "FAILED ✘"));
            System.out.println();
//            System.out.println(debug);
        }

        // ---- Summary section ----
        System.out.println("\n=========== SUMMARY ===========");
        System.out.println("Total tests : " + inputs.length);
        System.out.println("Passed      : " + passed);
        System.out.println("Failed      : " + failed);

        if (failed == 0) {
            System.out.println("Status      : ALL GOOD ✔✔✔");
        } else {
            System.out.println("Status      : CHECK FAILED CASES ✘");
        }
        System.out.println("================================");
    }
}

