package enigma.engine;

import enigma.shared.dto.CodeConfig;

/**
 * Engine API for coordinating machine loading, configuration and processing.
 *
 * <p>Implementations are responsible for loading machine specifications,
 * accepting user input for machine configuration, creating or assigning a
 * {@code Code} to the machine, and processing input text through the machine.</p>
 *
 * <p>Validation of loaded specifications and configurations should be done
 * at the engine boundary before delegating object construction to factories.</p>
 *
 * @since 1.0
 */
public interface Engine {

    /**
     * Load and initialize a machine from the given XML file path.
     * Implementations should validate the loaded specification and initialize
     * an internal {@code Machine} ready for processing.
     *
     * @param path file-system path to machine XML
     */
    void loadMachime(String path);

    /**
     * Supply or update machine data (for example wiring/config inputs) from
     * an external source. The exact semantics are implementation-specific.
     *
     * @param input arbitrary input string used by the engine to update machine state
     */
    void machineData(String input);

    /**
     * Enter manual code configuration mode (interactive or programmatic).
     * Implementations should prompt for or accept manual rotor/reflector/position
     * settings and apply them to the machine.
     */
    void codeManual(CodeConfig config);

    /**
     * Generate or assign a random code configuration and apply it to the machine.
     * The engine should ensure the selected configuration is valid for the
     * current machine specification.
     */
    void codeRandom();

    /**
     * Process the provided input string through the currently configured
     * machine/code and return the resulting output.
     *
     * @param input the input text to process
     * @return processed/transformed output text
     */
    String process(String input);

    /**
     * Return or print runtime statistics (usage, timing, or other metrics).
     * Exact format and destination are implementation-specific.
     */
    void statistics();
}
