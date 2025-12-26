package enigma.console;

import enigma.engine.Engine;

/**
 * Console interface for the Enigma machine user interface.
 *
 * <p><b>Module:</b> enigma-console (user interaction, no business logic)</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Handle user input via Scanner or similar I/O mechanisms</li>
 *   <li>Display machine data and processing results</li>
 *   <li>Delegate all business logic to {@link Engine}</li>
 * </ul>
 *
 * <h2>What Console Does NOT Do</h2>
 * <ul>
 *   <li>Does not validate XML or machine specifications (loader responsibility)</li>
 *   <li>Does not construct machine components (engine/factory responsibility)</li>
 *   <li>Does not implement encryption logic (machine responsibility)</li>
 * </ul>
 *
 * <p><b>Convention:</b> All user-facing numbering starts at 1 (rotor IDs, positions).</p>
 *
 * @since 1.0
 */
public interface Console {

    /**
     * Starts the main console loop:
     * - show menu
     * - read user command
     * - execute command
     * - repeat until Exit is chosen
     */
    void run();

    /**
     * Runs an end-to-end test using the specified XML file.
     * @param xmlPath path to the XML file defining the machine and test parameters
     */
    void runTest(String xmlPath);
}
