package enigma.engine.components.engine;

/**
 * Represents the Enigma engine interface that orchestrates machine operations.
 * The engine is a passive component that handles XML loading, code configuration,
 * message processing, and statistics tracking without knowledge of its caller.
 */
public interface Engine {

    /**
     * Loads machine configuration from an XML file.
     * Validates the XML content and creates the machine components.
     * Invalid XML results in an error without overriding the previous machine.
     * Valid XML fully overrides the machine and resets history.
     *
     * @param path the path to the XML configuration file
     */
    void loadXml(String path);

    /**
     * Processes machine data based on the provided input.
     *
     * @param input the input data to process
     */
    void machineData(String input);

    /**
     * Configures the machine code manually.
     * Requires rotor IDs, initial positions, reflector ID, and optional plug pairs.
     */
    void codeManual(/*Args*/);

    /**
     * Generates and applies a random valid code configuration.
     * The generated code is returned in compact format.
     */
    void codeRandom();

    /**
     * Processes a message through the Enigma machine.
     * Characters not in the alphabet are rejected.
     *
     * @param input the message to encrypt/decrypt
     * @return the encrypted/decrypted message
     */
    String process(String input);

    /**
     * Displays statistics including original code, used codes,
     * messages processed, and processing durations.
     */
    void statistics();
}
