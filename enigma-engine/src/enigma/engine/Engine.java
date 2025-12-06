package enigma.engine;

import enigma.machine.component.rotor.RotorImpl;
import enigma.shared.dto.MachineState;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.processTrace;

/**
 * Engine API for coordinating machine loading, configuration and processing.
 *
 * <p><b>Module:</b> enigma-engine (orchestration + validation, no UI)</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Load machine specifications from XML via {@link enigma.loader.Loader}</li>
 *   <li>Validate {@link CodeConfig} against loaded {@link enigma.shared.spec.MachineSpec}</li>
 *   <li>Build runtime {@link enigma.machine.component.code.Code} via factories</li>
 *   <li>Process messages and return {@link DebugTrace} DTOs</li>
 * </ul>
 *
 * <h2>What Engine Does NOT Do</h2>
 * <ul>
 *   <li>Does not perform I/O or UI interactions (console responsibility)</li>
 *   <li>Does not expose internal machine or component objects</li>
 *   <li>Does not modify XML or reorder wires (loader responsibility)</li>
 * </ul>
 *
 * <h2>Validation Boundary</h2>
 * <p>Engine validates runtime configuration (rotor IDs, positions, reflector ID)
 * and ensures they match the loaded spec. Factories assume inputs are valid
 * and focus on object construction.</p>
 *
 * @since 1.0
 */
public interface Engine {

    /**
     * Load and initialize a machine from the given XML file path.
     *
     * <p>Delegates to the configured {@link enigma.loader.Loader} to parse
     * and validate the XML. The loaded {@link enigma.shared.spec.MachineSpec}
     * is stored internally for later code construction.</p>
     *
     * @param path file-system path to machine XML
     * @throws RuntimeException if loading or validation fails
     */
    void loadMachine(String path);

    /**
     * Print or display current machine wiring/configuration data.
     *
     * <p>The exact output format and destination are implementation-specific.
     * Typically delegates to {@code machine.toString()} for detailed wiring display.</p>
     */
    MachineState machineData();

    /**
     * Configure the machine with a manual code configuration.
     *
     * <p>This is the primary method for setting up the machine with a specific
     * rotor arrangement, positions, and reflector. The configuration uses char-based
     * rotor positions (e.g., 'O', 'D', 'X') in left→right order.</p>
     *
     * <p>Engine validates the config before delegating to {@link enigma.engine.factory.CodeFactory}.</p>
     *
     * @param config code configuration specifying rotor IDs, positions, and reflector ID
     * @throws IllegalArgumentException if config is invalid
     */
    void configManual(CodeConfig config);

    /**
     * Generate a random, valid {@link CodeConfig} and apply it to the machine.
     *
     * <p>The engine samples rotor IDs, positions (as chars), and reflector ID
     * from the loaded spec using {@link java.security.SecureRandom}, then
     * delegates to {@link #configManual(CodeConfig)} for validation and construction.</p>
     *
     * @throws IllegalStateException if machine spec is not loaded
     */
    void configRandom();

    /**
     * Process the provided input string through the currently configured machine.
     *
     * <p>Each character is processed individually, generating a {@link enigma.shared.dto.tracer.SignalTrace}
     * for detailed step-by-step analysis. The output string and all traces are bundled
     * into a {@link DebugTrace} DTO.</p>
     *
     * @param input the input text to process (all chars must be in alphabet)
     * @return detailed debug trace of the processing steps
     * @throws IllegalStateException if machine is not configured
     * @throws IllegalArgumentException if input contains invalid characters
     */
    processTrace process(String input);

    /**
     * Return or print runtime statistics (usage, timing, or other metrics).
     *
     * <p>Exact format and destination are implementation-specific.
     * Currently a placeholder for future implementation.</p>
     */
    void statistics();
}
