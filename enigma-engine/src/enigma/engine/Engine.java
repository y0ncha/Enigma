package enigma.engine;

import enigma.loader.exception.EnigmaLoadingException;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.MachineState;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

/**
 * Coordinates machine loading, configuration, and message processing.
 *
 * <p>Provides validation, orchestration, and history tracking for Enigma machine operations.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * engine.loadMachine(path);
 * engine.configManual(codeConfig);
 * ProcessTrace trace = engine.process(input);
 * </pre>
 */
public interface Engine {

    /**
     * Load and initialize a machine specification from an XML file.
     *
     * @param path file-system path to machine XML
     * @throws RuntimeException if loading or validation fails
     */
    void loadMachine(String path);

    /**
     * Returns current machine state and configuration data.
     *
     * @return machine state snapshot
     */
    MachineState machineData();

    /**
     * Configure the machine with specific rotor arrangement and positions.
     *
     * <p>Rotor positions use left→right order.</p>
     *
     * @param config code configuration specifying rotor IDs, positions, and reflector ID
     * @throws IllegalArgumentException if config is invalid
     */
    void configManual(CodeConfig config);

    /**
     * Generate and apply a random configuration from loaded specification.
     *
     * @throws IllegalStateException if machine spec is not loaded
     */
    void configRandom();

    /**
     * Process input string through the configured machine.
     *
     * @param input the input text to process
     * @return processing trace with output and signal details
     * @throws IllegalStateException    if machine is not configured
     * @throws IllegalArgumentException if input contains invalid characters
     */
    ProcessTrace process(String input);

    /**
     * Returns the current machine specification loaded by the engine.
     *
     * @return the loaded {@link MachineSpec}, or {@code null} if no machine is loaded
     */
    MachineSpec getMachineSpec();

    /**
     * Returns the current code configuration applied to the machine.
     *
     * @return the current {@link CodeConfig}, or {@code null} if not configured
     */
    CodeConfig getCurrentCodeConfig();


    /**
     * Reset rotor positions to their original configuration.
     *
     * <p>Returns positions to initial values without changing rotor selection,
     * reflector, plugboard, history, or message counter.</p>
     *
     * @throws IllegalStateException if machine is not configured
     */
    void reset();

    /**
     * Returns formatted processing history.
     *
     * <p>History is grouped by original code configuration and persists
     * until a new machine is loaded or engine is terminated.</p>
     *
     * @return formatted history string
     */
    String history();

    /**
     * Clear all engine state and return to uninitialized state.
     *
     * <p>Does not terminate the application. Engine can be reused after calling this method.</p>
     */
    void terminate();

    void saveSnapshot(String basePath);

    void loadSnapshot(String basePath);
}