package test.enigma.engine.sanitysamll;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.ProcessTrace;

import java.nio.file.Paths;

/**
 * Single-case sanity tester for the Enigma engine.
 *
 * <p>Loads the small sanity XML, applies a single code configuration
 * and processes one input string, printing both the output and the
 * detailed {@link ProcessTrace}.</p>
 *
 * <p>Use this when you are debugging a specific mismatch (for example,
 * why "AABBCCDDEEFF" does not match the expected reference output).</p>
 */
public class SmallSingleWordTester {

    // Use the same test resources directory as the loader tests
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";
    private static final String XML_PATH     = Paths.get(XML_BASE_DIR, "ex1-sanity-small.xml").toString();

    // ---- Configure the single test case here ----
    private static final String INPUT          = "FEDCBADDEF";
    private static final String EXPECTED       = "ADEBCFEEDA";

    // Code: <3,2,1><CCC><I>
    private static final CodeConfig CODE_CONFIG = new CodeConfig(
            java.util.List.of(3, 2, 1),   // rotors left→right
            java.util.List.of('C', 'C', 'C'),   // "CCC" (A=0,B=1,C=2)
            "I"                           // reflector
    );

    /**
     * Entry point for the single-case sanity test.
     */
    public static void main(String[] args) {

        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH + "\n");
        try {
            engine.loadMachine(XML_PATH); // keep the current Engine API as-is
        }
        catch (Exception e) {
            System.err.println("Failed to load machine: " + e.getMessage());
            return;
        }

        System.out.println("Code configuration: " + CODE_CONFIG + "\n");
        engine.configManual(CODE_CONFIG);

        System.out.println("===== Single Sanity Case =====");
        System.out.println("Input   : " + INPUT);
        System.out.println("Expected: " + EXPECTED);
        System.out.println();

        ProcessTrace debug = engine.process(INPUT);

        String actual = debug.output();
        boolean ok = actual.equals(EXPECTED);

        System.out.println("Actual  : " + actual);
        System.out.println("Result  : " + (ok ? "PASSED" : "FAILED"));
        System.out.println();

        System.out.println(debug); // relies on ProcessTrace.toString() / pretty formatting
        System.out.println("------------------------");
    }
}