package test.enigma.engine.sanitypaper;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.DebugTrace;

import java.nio.file.Paths;

/**
 * Single-letter tester for the "sanity-paper" dataset.
 * Loads the sanity-paper XML, applies the code configuration
 * and processes a single input character, printing the
 * detailed {@link DebugTrace} to stdout.
 */
public class OneLetterTester {

    // Use the same test resources directory as the loader tests
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";
    private static final String XML_PATH     = Paths.get(XML_BASE_DIR, "ex1-sanity-paper-enigma.xml").toString();

    // ---- Configure the single-letter test case here ----
    private static final String INPUT    = "T"; // single letter input
    private static final String EXPECTED = "A";

    // Code: <1,2,3><ODX><I>
    private static final CodeConfig CODE_CONFIG = new CodeConfig(
            java.util.List.of(1, 2, 3),   // rotors left→right
            java.util.List.of(14, 3, 23), // "ODX" (O=14, D=3, X=23)
            "I"                           // reflector
    );

    public static void main(String[] args) {
        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH + "\n");
        engine.loadMachine(XML_PATH); // keep the current Engine API as-is

        System.out.println("Code configuration: " + CODE_CONFIG);
        engine.configManual(CODE_CONFIG);

        DebugTrace debug = engine.process(INPUT);
        System.out.println();

        // Print a concise result summary similar to the other sanity testers
        String actual = debug.output();
        boolean ok = EXPECTED.equals(actual);

        System.out.println("Input   : " + INPUT);
        System.out.println("Expected: " + EXPECTED);
        System.out.println("Actual  : " + actual);
        System.out.println("Result  : " + (ok ? "PASSED ✔" : "FAILED ✘"));
        System.out.println();

        System.out.println(debug); // relies on DebugTrace.toString() / pretty formatting
        System.out.println("------------------------");
    }
}
