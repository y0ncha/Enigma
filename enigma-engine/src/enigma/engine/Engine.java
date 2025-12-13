package enigma.engine;

import enigma.loader.exception.EnigmaLoadingException;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.MachineState;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

/**
 * Engine API — Coordinate loading, validation, and processing.
 * One-line: load machine spec, validate a CodeConfig and process messages.
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
     * Reset the machine to its original code configuration.
     *
     * <p><b>What This Does:</b></p>
     * <ul>
     *   <li>Returns rotor positions to their <b>original values</b> (as configured)</li>
     *   <li>Maintains rotor selection, reflector selection, and plugboard</li>
     *   <li>Maintains history and statistics</li>
     * </ul>
     *
     * <p><b>What This Does NOT Do:</b></p>
     * <ul>
     *   <li>❌ Clear processing history</li>
     *   <li>❌ Reset message counter</li>
     *   <li>❌ Change rotor or reflector selection</li>
     *   <li>❌ Create new original code (still grouped under same history key)</li>
     * </ul>
     *
     * <p><b>Use Case:</b> Process multiple messages from the same starting positions
     * without manually reconfiguring.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * engine.configManual(new CodeConfig(..., positions=['O','D','X'], ...));
     * engine.process("HELLO");  // positions advance to something else
     * engine.reset();           // positions return to ['O','D','X']
     * engine.process("WORLD");  // starts again from ['O','D','X']
     * </pre>
     *
     * @throws IllegalStateException if machine is not configured
     */
    void reset();

    /**
     * Returns a formatted string containing complete processing history.
     *
     * <p>History is grouped by original code configuration. Each group shows
     * all messages processed from that starting configuration, including:</p>
     * <ul>
     *   <li>Input message</li>
     *   <li>Output message</li>
     *   <li>Processing duration (nanoseconds)</li>
     * </ul>
     *
     * <p><b>History Grouping:</b> Messages are grouped by the original code
     * (rotor IDs, initial positions, reflector, plugboard) at configuration time.
     * All messages processed after a configuration are grouped together, even
     * though rotor positions change during processing.</p>
     *
     * <p><b>History Reset:</b> History is cleared only when:</p>
     * <ul>
     *   <li>New machine is loaded ({@link #loadMachine(String)})</li>
     *   <li>Engine is terminated ({@link #terminate()})</li>
     * </ul>
     *
     * <p>History is NOT cleared when reset is called.</p>
     *
     * @return formatted history string, or empty message if no history
     */
    String history();

    /**
     * Clear engine state and return to uninitialized state.
     *
     * <p><b>IMPORTANT:</b> Despite the name, this method does <b>NOT</b> terminate
     * the application or call {@code System.exit()}. It only clears internal engine state.</p>
     *
     * <p><b>What This Does:</b></p>
     * <ul>
     *   <li>Clears loaded machine specification</li>
     *   <li>Clears code configuration</li>
     *   <li>Clears processing history</li>
     *   <li>Resets message counter</li>
     *   <li>Returns engine to uninitialized state (as if newly created)</li>
     * </ul>
     *
     * <p><b>What This Does NOT Do:</b></p>
     * <ul>
     *   <li> Terminate the application</li>
     *   <li> Close resources (no resources to close)</li>
     *   <li> Prevent further use (engine can be reused after terminate)</li>
     * </ul>
     *
     * <p><b>Name Origin:</b> The name is historical and somewhat misleading.
     * Consider it as "clear state" or "reset engine completely".</p>
     *
     * <p><b>Use Case:</b> Start fresh without creating a new engine instance,
     * or clear sensitive data from memory.</p>
     *
     * <p><b>Example:</b></p>
     * <pre>
     * engine.loadMachine("enigma.xml");
     * engine.configManual(...);
     * engine.process("SECRET");
     * engine.terminate();  // Clear everything
     * // Engine is now uninitialized but can be used again
     * engine.loadMachine("other.xml");
     * </pre>
     */
    void terminate();

    void saveSnapshot(String basePath);

    void loadSnapshot(String basePath);
}