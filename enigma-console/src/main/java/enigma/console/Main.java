package enigma.console;

import enigma.engine.Engine;
import enigma.engine.EngineImpl;

/**
 * Entry point for the Enigma machine console application.
 *
 * <p><b>Module:</b> enigma-console</p>
 *
 * <p>This class bootstraps the console UI and delegates to the
 * {@link Engine} for all machine operations and business logic.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Initialize the console interface</li>
 *   <li>Start the user interaction loop</li>
 * </ul>
 *
 * @since 1.0
 */
public class Main {

    public static void main(String[] args) {
        Engine engine = new EngineImpl();
        // Create the console UI, passing the engine
        Console console = new ConsoleImpl(engine);
        // Start the console program loop
        console.run();
    }
}
