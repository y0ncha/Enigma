package test.enigma.engine;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.DebugTrace;

public class SimpleSanityTest {

    private static final String XML_PATH =
            "/Users/yonatan/Library/CloudStorage/OneDrive-TheAcademicCollegeofTel-AvivJaffa-MTA/GoodNotes/שנה ג/סמסטר א/Java E2E/Enigma/enigma-loader/src/test/resources/xml/ex1-sanity-small.xml";

    public static void main(String[] args) {

        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH);
        engine.loadMachime(XML_PATH);

        // Code: <3,2,1><CCC><I>
        CodeConfig config = new CodeConfig(
                java.util.List.of(3, 2, 1),   // rotors left→right
                java.util.List.of(2, 2, 2),   // "CCC" (A=0,B=1,C=2)
                "I"                           // reflector
        );
        engine.codeManual(config);

        // Sanity-small inputs & expected outputs from the appendix table
        String[] inputs = {
                "AABBCCDDEEFF",
                "FEDCBADDEF",
                "FEDCBAABCDEF",
                "AFBFCFDFEFFF",
                "AAAEEEBBBDDDCCCFFF"
        };

        String[] expectedOutputs = {
                "FFCCBBEEDDAA",
                "ADEBCFEEDA",
                "ADEBCFFCBEDA",
                "FACABAEADAAA",
                "FFFDDDCCCEEEAFEDCB"
        };

        for (int i = 0; i < inputs.length; i++) {
            // If you have a reset-to-original-code API, call it here, e.g.:
            // engine.reset();  // or engine.resetToOriginalCode();

            String input = inputs[i];
            DebugTrace debug = engine.processDebug(input);

            System.out.println("========== Sanity Case #" + (i + 1) + " ==========");
            System.out.println("Input   : " + input);
            System.out.println("Expected: " + expectedOutputs[i]);
            System.out.println("Actual  : " + debug.output());
            System.out.println();
            // Pretty printing handled by DebugTrace → SignalTrace.toString()
            System.out.println(debug);
        }
    }
}