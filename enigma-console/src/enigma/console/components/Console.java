package enigma.console.components;

import enigma.engine.components.engine.Engine;

/**
 * Represents the console user interface for the Enigma machine.
 * <p>
 * The Console is responsible for all user interaction, including
 * reading input from the user and displaying output. It delegates
 * all business logic to the Engine and contains no processing logic.
 * </p>
 * <p>
 * UI Guidelines:
 * </p>
 * <ul>
 *   <li>Uses Scanner for input reading</li>
 *   <li>All numbering starts at 1 for user interaction</li>
 *   <li>Errors are clear, readable, and actionable</li>
 *   <li>No colors, console clearing, or fancy output</li>
 * </ul>
 *
 * @see Engine
 */
public interface Console {
}
