package enigma.engine.components.loader;

/**
 * XML-based implementation of the Loader interface.
 * Handles parsing and validation of Enigma machine configuration from XML files.
 * Validates that:
 * <ul>
 *   <li>Alphabet length is even</li>
 *   <li>Rotor IDs are consecutive from 1 to N</li>
 *   <li>Reflectors have unique Roman numeral IDs</li>
 *   <li>Reflector does not map a letter to itself</li>
 *   <li>Rotor mappings are bijective</li>
 * </ul>
 */
public class LoadXml implements Loader {
}
