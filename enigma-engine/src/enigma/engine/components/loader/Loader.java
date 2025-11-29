package enigma.engine.components.loader;
import enigma.engine.components.model.MachineSpecification;

public interface Loader {
    /**
     * Loads and validates an Enigma machine definition from the given XML file.
     * On success returns MachineSpecification (alphabet, rotors, reflectors).
     * On failure throws EnigmaLoadingException with a clear message.
     */
    MachineSpecification loadMachine(String filePath) throws EnigmaLoadingException;
}
