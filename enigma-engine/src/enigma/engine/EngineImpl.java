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
 * Engine implementation — orchestration and validation delegation.
 *
 * One-line: load machine specs, validate configs and run the machine.
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

    // ---------------------------------------------------------
    // Engine API (as described in assignment / instructions)
    // ---------------------------------------------------------


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
            // TODO statistics/history reset is handled elsewhere if needed
        } catch (EnigmaLoadingException e) {
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
    public MachineState machineData() {
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
        EngineValidator.validateCodeConfig(spec, config);
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
     * @throws IllegalStateException if machine is not configured
     * @throws IllegalArgumentException if input is null or contains invalid characters
     */
    @Override
    public ProcessTrace process(String input) {
        if (!machine.isConfigured()) {
            throw new IllegalStateException("Machine is not configured");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null");
        }

        // Validate all characters are in the machine alphabet
        EngineValidator.validateInputInAlphabet(spec, input);

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

    @Override
    public void reset() {
        // TODO implement
        // machine.reset();
    }

    @Override
    public void terminate() {
        // TODO implement
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
        return spec;
    }

    @Override
    public CodeConfig getCurrentCodeConfig() {
        if (!machine.isConfigured()) {
            return null;
        }
        return machine.getConfig();
    }

    @Override
    public long getTotalProcessedMessages() {
        return stringsProcessed;
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
    @Override
    public void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        EngineValidator.validateCodeConfig(spec, config);
    }

    @Override
    public void validateNullChecks(List<Integer> rotorIds, List<Character> positions, String reflectorId) {
        EngineValidator.validateNullChecks(rotorIds, positions, reflectorId);
    }

    @Override
    public void validateRotorAndPositionCounts(List<Integer> rotorIds, List<Character> positions) {
        EngineValidator.validateRotorAndPositionCounts(rotorIds, positions);
    }

    @Override
    public void validateRotorIdsExistenceAndUniqueness(MachineSpec spec, List<Integer> rotorIds) {
        EngineValidator.validateRotorIdsExistenceAndUniqueness(spec, rotorIds);
    }

    @Override
    public void validateReflectorExists(MachineSpec spec, String reflectorId) {
        EngineValidator.validateReflectorExists(spec, reflectorId);
    }

    @Override
    public void validatePositionsInAlphabet(MachineSpec spec, List<Character> positions) {
        EngineValidator.validatePositionsInAlphabet(spec, positions);
    }

    @Override
    public void validateInputInAlphabet(MachineSpec spec, String input) {
        EngineValidator.validateInputInAlphabet(spec, input);
    }

}
