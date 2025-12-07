package enigma.engine;

import enigma.engine.factory.CodeFactoryImpl;
import enigma.loader.Loader;
import enigma.loader.EnigmaLoadingException;
import enigma.loader.LoaderXml;
import enigma.engine.factory.CodeFactory;
import enigma.machine.MachineImpl;
import enigma.machine.component.code.Code;
import enigma.machine.Machine;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.CodeState;
import enigma.shared.state.MachineState;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.SignalTrace;
import enigma.shared.spec.MachineSpec;

import java.security.SecureRandom;
import java.util.*;

/**
 * Default {@link Engine} implementation.
 *
 * <p><b>Module:</b> enigma-engine (orchestration + validation, no UI)</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Load machine specification (XML) via {@link Loader}</li>
 *   <li>Validate runtime {@link CodeConfig} (rotor IDs, positions, reflector)</li>
 *   <li>Build runtime {@link Code} using {@link CodeFactory}</li>
 *   <li>Assign code to internal {@link Machine} instance</li>
 *   <li>Process messages and return {@link ProcessTrace} DTOs</li>
 * </ul>
 *
 * <h2>Configuration Flow</h2>
 * <ol>
 *   <li>Load XML → {@link MachineSpec} (via loader)</li>
 *   <li>Manual or random config → {@link CodeConfig}</li>
 *   <li>Validate config against spec</li>
 *   <li>Build {@link Code} (via factory)</li>
 *   <li>Assign code to machine</li>
 * </ol>
 *
 * <h2>Validation Boundary</h2>
 * <p>Engine validates:</p>
 * <ul>
 *   <li>Number of rotors matches expected count (currently 3)</li>
 *   <li>Rotor IDs are unique and exist in spec</li>
 *   <li>Reflector ID exists in spec</li>
 *   <li>Position characters are valid alphabet members</li>
 * </ul>
 * <p>Factories assume inputs are valid and focus on object construction.</p>
 *
 * <h2>Rotor Position Model</h2>
 * <p>Positions in {@link CodeConfig} are characters from the alphabet
 * (e.g., 'O', 'D', 'X') in left→right order. The factory and machine
 * preserve this ordering throughout construction and operation.</p>
 *
 * <h2>What Engine Does NOT Do</h2>
 * <ul>
 *   <li>Does not perform I/O or printing (except getState for diagnostics)</li>
 *   <li>Does not expose internal machine or component objects</li>
 *   <li>Does not revalidate spec contents (loader responsibility)</li>
 * </ul>
 *
 * @since 1.0
 */
public class EngineImpl implements Engine {

    private static final int ROTORS_IN_USE = 3; // Can be dynamically configured in the future

    private final Machine machine;
    private final Loader loader;
    private final CodeFactory codeFactory;

    private MachineSpec spec;
    private CodeState ogCodeState;
    private int stringsProcessed = 0; // number of processed messages (snapshot counter)

    /**
     * Construct an Engine that uses the default XML {@link Loader} and
     * the default {@link CodeFactory} implementation.
     *
     * <p>The engine creates an internal {@link Machine} instance which
     * is configured later when a {@link Code} is assigned (manual or random).</p>
     */
    public EngineImpl() {
        this.machine = new MachineImpl();
        this.loader = new LoaderXml(ROTORS_IN_USE);
        this.codeFactory = new CodeFactoryImpl();
    }

    /**
     * Load and validate a machine specification from an XML file and store it in the engine.
     *
     * <p>Example:
     * <pre>engine.loadMachine("enigma-loader/src/test/resources/xml/ex1-sanity-paper-enigma.xml");</pre>
     *
     * Important (concise):
     * - Does NOT configure the runtime {@link Machine}; call {@link #configManual(CodeConfig)} or {@link #configRandom()} to apply a {@link Code}.
     * - The {@link Loader} performs schema and structural validation (alphabet, rotors, reflector pairs).
     * - Caller must handle concurrency; the last successful load overwrites the engine spec.
     *
     * @param path absolute or relative path to the Enigma XML file
     * @throws RuntimeException if parsing or validation fails (loader error wrapped)
     */
    @Override
    public void loadMachine(String path) {
        try {
            spec = loader.loadSpecs(path);
        }
        catch (EnigmaLoadingException e) {
            throw new RuntimeException("Failed to load machine XML: " + e.getMessage(), e);
        }
    }

    /**
     * Print detailed machine wiring information to System.out.
     *
     * <p>Delegates to {@code machine.toString()} which displays index column,
     * reflector pairs, rotor wirings, and keyboard mapping.</p>
     */
    @Override
    public MachineState getState() {
        int rotors = spec == null ? 0 : spec.rotorsById().size();
        int reflectors = spec == null ? 0 : spec.reflectorsById().size();
        CodeState currCodeState = machine.getCodeState();
        return new MachineState(rotors, reflectors, stringsProcessed, this.ogCodeState, currCodeState );
    }

    /**
     * Configure machine with a manual {@link CodeConfig}.
     *
     * <p>Validates the config against the loaded spec, then delegates to
     * {@link CodeFactory#create(MachineSpec, CodeConfig)} to build the runtime
     * {@link Code}. The code is assigned to the internal machine via
     * {@link Machine#setCode(Code)}.</p>
     *
     * @param config configuration with rotor IDs, positions (chars), reflector ID
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if spec is not loaded
     */
    @Override
    public void configManual(CodeConfig config) {
        validateCodeConfig(spec, config);
        Code code = codeFactory.create(spec, config);
        machine.setCode(code);
        ogCodeState = machine.getCodeState();
    }


    /**
     * Generate a random, valid {@link CodeConfig} and configure the machine.
     *
     * <p>Sampling strategy:</p>
     * <ul>
     *   <li>Pick {@value #ROTORS_IN_USE} unique rotor IDs (left→right)</li>
     *   <li>Generate random char positions (left→right) from alphabet</li>
     *   <li>Pick one random reflector ID</li>
     * </ul>
     * <p>Delegates to {@link #configManual(CodeConfig)} for validation and construction.</p>
     *
     * @throws IllegalStateException when spec is not loaded
     */
    @Override
    public void configRandom() {
        CodeConfig config = generateRandomConfig(spec);
        configManual(config);
    }

    /**
     * Process the provided input string through the currently configured
     * machine/code with detailed debugging information.
     *
     * @param input the input text to process
     * @return detailed debug trace of the processing steps
     */
    @Override
    public ProcessTrace process(String input) {
        if (!machine.isConfigured()) {
            throw new IllegalStateException("Machine is not configured");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null");
        }

        List<SignalTrace> traces = new ArrayList<>();
        StringBuilder output = new StringBuilder();

        for (char c : input.toCharArray()) {
            SignalTrace trace = machine.process(c);
            traces.add(trace);
            output.append(trace.outputChar());
        }
        this.stringsProcessed++;
        return new ProcessTrace(output.toString(), List.copyOf(traces));
    }

    /**
     * Produce runtime statistics for the engine.
     *
     * <p>Currently a placeholder; implementation may be added later.</p>
     */
    @Override
    public void statistics() {
        // TODO implement
    }

    @Override
    public MachineSpec getMachineSpec() {
        // TODO implement
        return null;
    }

    @Override
    public CodeConfig getCurrentCodeConfig() {
        // TODO implement
        return null;
    }

    @Override
    public long getTotalProcessedMessages() {
        // TODO implement
        return 0;
    }

    // ---------------------------------------------------------
    // Flow helpers: machine creation and random code generation
    // ---------------------------------------------------------

    /**
     * Generate a random {@link CodeConfig} from the loaded {@link MachineSpec}.
     *
     * <p>Sampling rules:</p>
     * <ul>
     *   <li>Pick exactly {@link #ROTORS_IN_USE} unique rotor IDs (left→right)</li>
     *   <li>Pick one reflector ID at random</li>
     *   <li>Generate random starting positions as chars (left→right)</li>
     * </ul>
     * <p>The returned {@link CodeConfig} uses left→right ordering for rotors and
     * positions (user-facing conventions).</p>
     *
     * @param spec machine specification (must be non-null)
     * @return randomly sampled {@link CodeConfig}
     * @throws IllegalStateException when {@code spec} is null
     */
    private CodeConfig generateRandomConfig(MachineSpec spec) {
        if (spec == null) {
            throw new IllegalStateException(
                    "Machine is not loaded. Load an XML file before generating a random code.");
        }

        SecureRandom random = new SecureRandom();

        // Shuffle available rotor IDs and pick exactly ROTORS_IN_USE of them in random order (left → right)
        List<Integer> rotorPool = new ArrayList<>(spec.rotorsById().keySet());
        Collections.shuffle(rotorPool, random);
        List<Integer> chosenRotors = new ArrayList<>(rotorPool.subList(0, ROTORS_IN_USE)); // left→right

        // Generate random starting positions as characters (left → right)
        int alphaSize = spec.alphabet().size();
        List<Character> positions = new ArrayList<>(ROTORS_IN_USE);
        for (int i = 0; i < ROTORS_IN_USE; i++) {
            int randIndex = random.nextInt(alphaSize);        // 0 .. alphaSize-1
            char posChar = spec.alphabet().charAt(randIndex); // map index → symbol
            positions.add(posChar);
        }

        // Pick a random reflector
        List<String> reflectorIds = new ArrayList<>(spec.reflectorsById().keySet());
        String reflectorId = reflectorIds.get(random.nextInt(reflectorIds.size()));

        // rotorIds (left→right), positions as chars (left→right), reflectorId
        return new CodeConfig(chosenRotors, positions, reflectorId);
    }

    // ---------------------------------------------------------
    // Engine-level validation helpers
    // ---------------------------------------------------------

    /**
     * Validate a {@link CodeConfig} against the (already-loaded) {@link MachineSpec}.
     *
     * <p>Orchestrates validation by calling targeted validators in sequence:</p>
     * <ul>
     *   <li>{@link #validateNullChecks(List, List, String)} — null checks</li>
     *   <li>{@link #validateRotorAndPositionCounts(List, List)} — count validation</li>
     *   <li>{@link #validateRotorIdsExistenceAndUniqueness(MachineSpec, List)} — rotor validation</li>
     *   <li>{@link #validateReflectorExists(MachineSpec, String)} — reflector validation</li>
     *   <li>{@link #validatePositionsInAlphabet(MachineSpec, List)} — position validation</li>
     * </ul>
     * <p>Spec contents are assumed valid (loader responsibility).</p>
     *
     * @param spec   machine specification (assumed valid by the loader)
     * @param config code configuration to validate
     * @throws IllegalArgumentException when validation fails
     */
    private void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        if (config == null) throw new IllegalArgumentException("CodeConfig must not be null");

        List<Integer> rotorIds = config.rotorIds();
        List<Character> positions = config.positions();
        String reflectorId = config.reflectorId();

        validateNullChecks(rotorIds, positions, reflectorId);
        validateRotorAndPositionCounts(rotorIds, positions);
        validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
        validateReflectorExists(spec, reflectorId);
        validatePositionsInAlphabet(spec, positions);
    }

    /**
     * Validate that config fields are not null.
     *
     * @param rotorIds    rotor IDs from config
     * @param positions   positions from config
     * @param reflectorId reflector ID from config
     * @throws IllegalArgumentException if any required parameter is null
     */
    private void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId) {
        if (rotorIds == null) throw new IllegalArgumentException("rotorIds must not be null");
        if (positions == null) throw new IllegalArgumentException("positions must not be null");
        if (reflectorId == null) throw new IllegalArgumentException("reflectorId must not be null");
    }

    /**
     * Validate that rotor and position counts match expected count.
     *
     * @param rotorIds  list of rotor IDs
     * @param positions list of initial positions
     * @throws IllegalArgumentException if counts do not match {@link #ROTORS_IN_USE}
     */
    private void validateRotorAndPositionCounts(List<Integer> rotorIds, List<Character> positions) {
        if (rotorIds.size() != ROTORS_IN_USE) throw new IllegalArgumentException("Exactly " + ROTORS_IN_USE + " rotors must be selected");
        if (positions.size() != ROTORS_IN_USE) throw new IllegalArgumentException("Exactly " + ROTORS_IN_USE + " initial positions must be provided");
    }

    /**
     * Validate that rotor IDs are unique and exist in spec.
     *
     * @param spec     machine specification
     * @param rotorIds list of rotor IDs
     * @throws IllegalArgumentException if rotor IDs are duplicated or do not exist
     */
    private void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        // Do not re-validate the spec contents here — loader guarantees valid spec.

        Set<Integer> seen = new HashSet<>();
        for (int id : rotorIds) {
            if (!seen.add(id)) throw new IllegalArgumentException("Duplicate rotor " + id);
            if (spec.getRotorById(id) == null) throw new IllegalArgumentException("Rotor " + id + " does not exist in spec");
        }
    }

    /**
     * Validate that reflector ID exists in spec.
     *
     * @param spec        machine specification
     * @param reflectorId reflector identifier
     * @throws IllegalArgumentException if reflector is blank or does not exist
     */
    private void validateReflectorExists(MachineSpec spec, String reflectorId) {
        if (reflectorId.isBlank()) throw new IllegalArgumentException("reflectorId must be non-empty");
        if (spec.getReflectorById(reflectorId) == null) throw new IllegalArgumentException("Reflector '" + reflectorId + "' does not exist");
    }

    /**
     * Validate that all position characters are valid alphabet members.
     *
     * @param spec      machine specification
     * @param positions list of initial positions
     * @throws IllegalArgumentException if any position character is not in alphabet
     */
    private void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions) {
        for (char c : positions) {
            if (!spec.alphabet().contains(c)) throw new IllegalArgumentException(c + " is not a valid position");
        }
    }
}
