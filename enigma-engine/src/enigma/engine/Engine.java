package enigma.engine;

import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.MachineState;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.util.List;

/**
 * Engine API — Coordinate loading, validation and processing.
 *
 * One-line: load machine spec, validate a CodeConfig and process messages.
 *
 * Usage:
 * <pre>
 * engine.loadMachine(path);
 * engine.configManual(codeConfig);
 * ProcessTrace trace = engine.process(input);
 * </pre>
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
     * into a {@link ProcessTrace} DTO.</p>
     *
     * @param input the input text to process (all chars must be in alphabet)
     * @return detailed debug trace of the processing steps
     * @throws IllegalStateException    if machine is not configured
     * @throws IllegalArgumentException if input contains invalid characters
     */
    ProcessTrace process(String input);

    void reset();

    void terminate();

    /**
     * Return or print runtime statistics (usage, timing, or other metrics).
     *
     * <p>Exact format and destination are implementation-specific.
     * Currently a placeholder for future implementation.</p>
     */
    void statistics();

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
     * Returns the total number of messages processed by the engine since initialization.
     *
     * @return the total count of processed messages (non-negative)
     */
    long getTotalProcessedMessages();

    void validateCodeConfig(MachineSpec spec, CodeConfig config);

    void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId);

    void validateRotorAndPositionCounts(List<Integer> rotorIds, List<Character> positions);

    void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds);

    void validateReflectorExists(MachineSpec spec, String reflectorId);

    void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions);

    void validateInputInAlphabet(MachineSpec spec, String input);
}