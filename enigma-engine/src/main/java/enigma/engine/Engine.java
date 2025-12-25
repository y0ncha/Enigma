package enigma.engine;

import enigma.engine.exception.MachineNotLoadedException;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.MachineState;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

/**
 * Engine API for loading, configuring, and processing messages.
 *
 * <p>Coordinates machine loading, validation, configuration, and message
 * processing. Maintains history and provides snapshot functionality.</p>
 *
 * @since 1.0
 */
public interface Engine {

    /**
     * Load machine specification from XML file.
     *
     * @param path file path to machine XML
     * @throws Exception if loading or validation fails
     */
    void loadMachine(String path) throws Exception;

    /**
     * Return current machine configuration and wiring data.
     *
     * @return machine state information
     */
    MachineState machineData();

    /**
     * Configure machine with manual rotor and reflector selection.
     *
     * @param config code configuration with rotor IDs, positions, and reflector
     * @throws IllegalArgumentException if configuration is invalid
     */
    void configManual(CodeConfig config);

    /**
     * Generate and apply random valid configuration.
     *
     * @throws IllegalStateException if machine specification is not loaded
     */
    void configRandom();

    /**
     * Process input string through configured machine.
     *
     * @param input text to process
     * @return detailed trace of processing steps
     * @throws IllegalStateException if machine is not configured
     * @throws IllegalArgumentException if input contains invalid characters
     */
    ProcessTrace process(String input);

    /**
     * Return loaded machine specification.
     *
     * @return machine spec, or null if not loaded
     */
    MachineSpec getMachineSpec();

    /**
     * Return current code configuration.
     *
     * @return code config, or null if not configured
     */
    CodeConfig getCurrentCodeConfig();


    /**
     * Reset rotor positions to original configuration values.
     *
     * <p>Returns positions to their initial values at configuration time.
     * Maintains rotor selection, reflector, and history.</p>
     *
     * @throws IllegalStateException if machine is not configured
     */
    void reset();

    /**
     * Return formatted processing history.
     *
     * <p>History is grouped by original code configuration and includes
     * input, output, and processing duration for each message.</p>
     *
     * @return formatted history string, or empty if no history
     */
    String history();

    /**
     * Clear all engine state and return to uninitialized state.
     *
     * <p>Clears machine specification, configuration, and history.
     * Does not terminate application or prevent reuse.</p>
     */
    void terminate();

    /**
     * Set up the plugboard connections from a string of character pairs.
     *
     * <p>Example: "ABCD" connects A↔B and C↔D.</p>
     *
     * @param connections string of character pairs for plugboard connections
     * @throws MachineNotLoadedException if no machine is loaded
     */
    void setPlugboard(String connections);

    /**
     * Save current engine state to snapshot file.
     *
     * @param basePath base file path for snapshot
     */
    void saveSnapshot(String basePath);

    /**
     * Load engine state from snapshot file.
     *
     * @param basePath base file path for snapshot
     */
    void loadSnapshot(String basePath);

    /**
     * Returns a human-readable representation of the currently configured machine.
     *
     * <p>
     * Implementations should return a concise, read-only textual representation
     * of the runtime machine (rotors, reflector and current rotor positions).
     * This method is intended for debugging and console UIs.
     * If no machine is loaded or the machine is not configured, returns
     * an appropriate message indicating the state.
     * </p>
     *
     * @return machine details string
     */
    String getMachineDetails();
}