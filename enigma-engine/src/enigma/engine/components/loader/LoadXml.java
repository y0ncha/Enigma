package enigma.engine.components.loader;

/**
 * XML-based implementation of the Enigma machine configuration loader.
 * <p>
 * This class parses and validates XML configuration files containing
 * the Enigma machine specifications. The XML must be schema-valid and
 * pass additional application-level validations:
 * </p>
 * <ul>
 *   <li>Even alphabet length</li>
 *   <li>Consecutive rotor IDs (1..N)</li>
 *   <li>Unique Roman numeral reflector IDs</li>
 *   <li>Reflectors cannot map a letter to itself</li>
 *   <li>Rotor mappings must be bijective (no duplicates)</li>
 * </ul>
 * <p>
 * Invalid XML returns a clear error without overriding the previous machine.
 * Valid XML fully overrides the machine configuration and resets history.
 * </p>
 */
public class LoadXml implements Loader {
}
