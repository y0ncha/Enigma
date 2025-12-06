package enigma.console;
import enigma.engine.Engine;
import enigma.engine.EngineImpl;

import java.util.Scanner;

/**
 * Application entry point for Exercise 1.
 */
public class Main {

    public static void main(String[] args) {
        Engine engine = new EngineImpl();

        Scanner scanner = new Scanner(System.in);
        Console console = new ConsoleImpl(engine, scanner);
        console.run();
    }

}
