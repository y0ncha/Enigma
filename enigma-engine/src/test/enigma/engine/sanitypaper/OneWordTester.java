package test.enigma.engine.sanitypaper;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.DebugTrace;

import java.nio.file.Paths;

/**
 * Single-case sanity tester for the "sanity-paper" dataset.
 * Loads the sanity-paper XML, applies a single code configuration
 * and processes one input string, printing both the output and the
 * detailed {@link DebugTrace}.
 */
public class OneWordTester {

    // Use the same test resources directory as the loader tests
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";
    private static final String XML_PATH     = Paths.get(XML_BASE_DIR, "ex1-sanity-paper-enigma.xml").toString();

    // ---- Configure the single test case here ----
    private static final String INPUT          = "THERAINISDROPPING";
    private static final String EXPECTED       = "APZTICDXRVMWQHBHU";

    // Code: <1,2,3><ODX><I>
    private static final CodeConfig CODE_CONFIG = new CodeConfig(
            java.util.List.of(1, 2, 3),   // rotors left→right
            java.util.List.of(14, 3, 23), // "ODX" (O=14, D=3, X=23)
            "I"                           // reflector
    );

    /**
     * Entry point for the single-case sanity test.
     */
    public static void main(String[] args) {

        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH + "\n");
        engine.loadMachime(XML_PATH); // keep the current Engine API as-is

        System.out.println("Code configuration: " + CODE_CONFIG + "\n");
        engine.codeManual(CODE_CONFIG);

        System.out.println("===== Single Sanity Case =====");
        System.out.println("Input   : " + INPUT);
        System.out.println("Expected: " + EXPECTED);
        System.out.println();

        DebugTrace debug = engine.process(INPUT);

        String actual = debug.output();
        boolean ok = actual.equals(EXPECTED);

        System.out.println("Actual  : " + actual);
        System.out.println("Result  : " + (ok ? "PASSED" : "FAILED"));
        System.out.println();

        System.out.println(debug); // relies on DebugTrace.toString() / pretty formatting
        System.out.println("------------------------");
    }
}

