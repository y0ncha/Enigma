package enigma.loader;

import enigma.loader.exception.EnigmaLoadingException;
import enigma.shared.spec.MachineSpec;

/**
 * Loader interface for reading Enigma machine specifications.
 *
 * <p><b>Module:</b> enigma-loader (XML parsing → MachineSpec)</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Parse machine definitions from external sources (e.g., XML)</li>
 *   <li>Validate alphabet, rotor, and reflector specifications</li>
 *   <li>Produce a validated {@link MachineSpec}</li>
 * </ul>
 *
 * <h2>Validation Rules</h2>
 * <ul>
 *   <li>Alphabet must be even-length, non-empty, with unique characters</li>
 *   <li>Rotor IDs must form contiguous sequence 1..N</li>
 *   <li>Reflector IDs must be Roman numerals (I, II, III, ...) starting from I</li>
 *   <li>Rotor columns must be full permutations (bijectivity)</li>
 *   <li>Reflector mappings must be symmetric and cover all indices</li>
 * </ul>
 *
 * <h2>Wiring Order</h2>
 * <p><b>Critical:</b> Loader must NOT reorder wires or change XML-defined order.
 * Rotor columns and reflector pairs are stored exactly as defined in XML.</p>
 *
 * @since 1.0
 */
public interface Loader {
    /**
     * Load and validate an Enigma machine definition from the given XML file.
     *
     * <p>On success returns {@link MachineSpec} (alphabet, rotors, reflectors).
     * On failure throws {@link EnigmaLoadingException} with a clear error message.</p>
     *
     * @param filePath path to the XML file
     * @return validated machine specification
     * @throws EnigmaLoadingException when loading or validation fails
     */
    MachineSpec loadSpecs(String filePath) throws EnigmaLoadingException;


}
