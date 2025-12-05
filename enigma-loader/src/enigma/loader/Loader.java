package enigma.loader;
import enigma.loader.xml.exception.EnigmaLoadingException;
import enigma.shared.spec.MachineSpec;

/**
 * Loader interface for reading Enigma machine specifications.
 *
 * <p>Implementations read machine definitions from external sources
 * (e.g., XML files) and produce a validated {@link MachineSpec}.</p>
 *
 * @since 1.0
 */
public interface Loader {
    /**
     * Loads and validates an Enigma machine definition from the given XML file.
     * On success returns MachineSpec (alphabet, rotors, reflectors).
     * On failure throws EnigmaLoadingException with a clear message.
     *
     * @param filePath path to the XML file
     * @return validated machine specification
     * @throws EnigmaLoadingException when loading or validation fails
     */
    MachineSpec loadSpecs(String filePath) throws EnigmaLoadingException;


}
