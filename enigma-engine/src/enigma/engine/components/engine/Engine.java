package enigma.engine.components.engine;

/**
 * Represents the Enigma engine interface.
 * <p>
 * The Engine serves as the orchestration layer between the console (UI)
 * and the machine (core logic). It handles XML loading, code configuration,
 * message processing, and statistics tracking. The engine is passive and
 * does not know its caller.
 * </p>
 */
public interface Engine {

    /**
     * Loads the Enigma machine configuration from an XML file.
     * <p>
     * The XML file contains the alphabet, rotors, and reflectors definitions.
     * A valid load resets history and fully overrides the machine configuration.
     * An invalid load returns a clear error without affecting the current state.
     * </p>
     *
     * @param path the file path to the XML configuration file
     */
    void loadXml(String path);

    /**
     * Retrieves and displays machine data/specifications.
     * <p>
     * Shows information about the loaded machine configuration
     * based on the input parameter.
     * </p>
     *
     * @param input the type of machine data to retrieve
     */
    void machineData(String input);

    /**
     * Configures the machine code manually.
     * <p>
     * Allows manual selection of rotors, initial positions, reflector,
     * and plug pairs. The rotor order is specified from right to left.
     * </p>
     * <p>
     * Note: Parameters will be added in future implementation.
     * </p>
     */
    void codeManual(/*Args*/);

    /**
     * Configures the machine with a random code.
     * <p>
     * Generates a random valid code configuration including
     * rotor selection, positions, reflector, and plug pairs.
     * The generated code is returned in a compact format.
     * </p>
     */
    void codeRandom();

    /**
     * Processes an input string through the Enigma machine.
     * <p>
     * Each character is encrypted/decrypted individually, advancing
     * the rotors according to the stepping mechanism. Characters not
     * in the alphabet are rejected.
     * </p>
     *
     * @param input the message to encrypt/decrypt
     * @return the encrypted/decrypted message
     */
    String process(String input);

    /**
     * Retrieves and displays usage statistics.
     * <p>
     * Shows history including all used codes, processed messages,
     * and timing information (in nanoseconds), grouped by code.
     * </p>
     */
    void statistics();
}
