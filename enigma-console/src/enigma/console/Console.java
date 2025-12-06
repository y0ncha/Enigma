package enigma.console;

/**
 * Console interface for the Enigma machine user interface.
 *
 * <p><b>Module:</b> enigma-console (user interaction, no business logic)</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Handle user input via Scanner or similar I/O mechanisms</li>
 *   <li>Display machine data and processing results</li>
 *   <li>Delegate all business logic to {@link enigma.engine.Engine}</li>
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
}
