package enigma.engine.components.loader;
import enigma.engine.components.model.MachineSpec;

public interface Loader {
    /**
     * Loads and validates an Enigma machine definition from the given XML file.
     * On success returns MachineSpec (alphabet, rotors, reflectors).
     * On failure throws EnigmaLoadingException with a clear message.
     */
    MachineSpec loadMachine(String filePath) throws EnigmaLoadingException;
}
