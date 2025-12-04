package test.enigma.engine;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.DebugTrace;

import java.nio.file.Paths;

/**
 * Single-letter tester for the Enigma engine.
 * Loads the small sanity XML, applies a code configuration and processes a single
 * input character, printing both the output and the detailed {@link DebugTrace}.
 */
public class EngineOneLetterTester {

    // Use the same test resources directory as the loader tests
    private static final String XML_BASE_DIR = "enigma-loader/src/test/resources/xml";
    private static final String XML_PATH     = Paths.get(XML_BASE_DIR, "ex1-sanity-small.xml").toString();

    // ---- Configure the single-letter test case here ----
    private static final String INPUT    = "A"; // single letter input

    // Code: <3,2,1><CCC><I>
    private static final CodeConfig CODE_CONFIG = new CodeConfig(
            java.util.List.of(3, 2, 1),   // rotors left→right
            java.util.List.of(2, 2, 2),   // "CCC" (A=0,B=1,C=2)
            "I"                           // reflector
    );

    public static void main(String[] args) {
        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH + "\n");
        engine.loadMachime(XML_PATH); // keep the current Engine API as-is

        System.out.println("Code configuration: " + CODE_CONFIG);
        engine.codeManualVirtual(CODE_CONFIG);

        DebugTrace debug = engine.processDebug(INPUT);
        System.out.println();

        System.out.println(debug); // relies on DebugTrace.toString() / pretty formatting
        System.out.println("------------------------");
    }
}

