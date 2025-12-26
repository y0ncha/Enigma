package enigma.console;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;

public class Tester {

    private static final String path = "/Users/yonatan/Library/CloudStorage/OneDrive-TheAcademicCollegeofTel-AvivJaffa-MTA/GoodNotes/שנה ג/סמסטר א/Java E2E/Enigma/enigma-console/src/test/resources/ex1-xml/ex1-sanity-paper-enigma.xml";
    public static void main(String[] args) {
        Engine engine = new EngineImpl();
        Console console = new ConsoleImpl(engine);
        console.runTest(path);
    }
}
