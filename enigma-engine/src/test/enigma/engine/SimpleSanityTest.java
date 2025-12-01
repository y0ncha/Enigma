package test.enigma.engine;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;
import enigma.shared.dto.tracer.DebugTrace;
import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.dto.tracer.RotorTrace;
import enigma.shared.dto.tracer.ReflectorTrace;

import java.util.List;

public class SimpleSanityTest {

    private static final String XML_PATH =
            "/Users/yonatan/Library/CloudStorage/OneDrive-TheAcademicCollegeofTel-AvivJaffa-MTA/GoodNotes/שנה ג/סמסטר א/Java E2E/Enigma/enigma-loader/src/test/resources/xml/ex1-sanity-small.xml";

    public static void main(String[] args) {

        Engine engine = new EngineImpl();

        System.out.println("Loading XML: " + XML_PATH);
        engine.loadMachime(XML_PATH);

        String input = "HELLO";
        System.out.println("Processing in DEBUG mode: " + input);

        DebugTrace debug = engine.processDebug(input);

        System.out.println("Output: " + debug.output());
        System.out.println("============================================");

        List<SignalTrace> signals = debug.signalTraces();

        for (int i = 0; i < signals.size(); i++) {
            SignalTrace s = signals.get(i);

            System.out.println("----- Character #" + (i + 1) + " -----");
            System.out.println("Input  : " + s.inputChar());
            System.out.println("Output : " + s.outputChar());
            System.out.println("Window : before=" + s.windowBefore() +
                    " after=" + s.windowAfter());
            System.out.println("Stepped: " + s.advancedIndices());
            System.out.println();

            System.out.println("Forward path (right → left):");
            printRotorPath(s.forwardSteps());

            System.out.println("Reflector:");
            printReflectorStep(s.reflectorStep());

            System.out.println("Backward path (left → right):");
            printRotorPath(s.backwardSteps());

            System.out.println();
        }
    }

    private static void printRotorPath(List<RotorTrace> steps) {
        for (RotorTrace r : steps) {
            System.out.printf(
                    "  rotor %d: %c(%d) -> %c(%d)%n",
                    r.rotorIndex(),
                    r.entryChar(), r.entryIndex(),
                    r.exitChar(), r.exitIndex()
            );
        }
    }

    private static void printReflectorStep(ReflectorTrace ref) {
        System.out.printf(
                "  %c(%d) -> %c(%d)%n",
                ref.entryChar(), ref.entryIndex(),
                ref.exitChar(), ref.exitIndex()
        );
    }
}