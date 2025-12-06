package enigma.engine;

import enigma.engine.factory.CodeFactoryImpl;
import enigma.loader.Loader;
import enigma.loader.EnigmaLoadingException;
import enigma.loader.LoaderXml;
import enigma.engine.factory.CodeFactory;
import enigma.machine.MachineImpl;
import enigma.machine.component.code.Code;
import enigma.machine.Machine;
import enigma.machine.component.rotor.RotorImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.dto.tracer.DebugTrace;
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
 *   <li>Process messages and return {@link DebugTrace} DTOs</li>
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
 *   <li>Does not perform I/O or printing (except machineData for diagnostics)</li>
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
    private CodeConfig origConfig;

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
     * Load machine specification from XML file path.
     *
     * <p>Delegates to {@link Loader#loadSpecs(String)} which parses and
     * validates the XML. The resulting {@link MachineSpec} is stored for
     * later code construction.</p>
     *
     * @param path file-system path to XML file
     * @throws RuntimeException wrapping {@link EnigmaLoadingException} on failure
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
    public void machineData() {
        System.out.println(machine);
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
        if (origConfig == null) origConfig = config;
        machine.setCode(code);
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
        CodeConfig config = randomCodeConfig(spec);
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
    public DebugTrace process(String input) {
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
        return new DebugTrace(output.toString(), List.copyOf(traces));
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
    private CodeConfig randomCodeConfig(MachineSpec spec) {
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
     * <p>Checks performed:</p>
     * <ul>
     *   <li>Number of rotors/positions equals {@link #ROTORS_IN_USE}</li>
     *   <li>Rotor IDs are unique and exist in spec</li>
     *   <li>Reflector ID exists in spec</li>
     *   <li>Position chars are valid alphabet members</li>
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
        List<Character> positions = config.initialPositions();
        String reflectorId = config.reflectorId();

        if (rotorIds == null) throw new IllegalArgumentException("rotorIds must not be null");
        if (positions == null) throw new IllegalArgumentException("initialPositions must not be null");
        if (reflectorId == null) throw new IllegalArgumentException("reflectorId must not be null");

        if (rotorIds.size() != ROTORS_IN_USE) throw new IllegalArgumentException("Exactly " + ROTORS_IN_USE + " rotors must be selected");
        if (positions.size() != ROTORS_IN_USE) throw new IllegalArgumentException("Exactly " + ROTORS_IN_USE + " initial positions must be provided");

        // Do not re-validate the spec contents here — loader guarantees valid spec.

        Set<Integer> seen = new HashSet<>();
        for (int id : rotorIds) {
            if (!seen.add(id)) throw new IllegalArgumentException("Duplicate rotor " + id);
            if (spec.getRotorById(id) == null) throw new IllegalArgumentException("Rotor " + id + " does not exist in spec");
        }

        if (reflectorId.isBlank()) throw new IllegalArgumentException("reflectorId must be non-empty");
        if (spec.getReflectorById(reflectorId) == null) throw new IllegalArgumentException("Reflector '" + reflectorId + "' does not exist");

        for (char c : positions) {
            if (!spec.alphabet().contains(c)) throw new IllegalArgumentException(c + " is not a valid position");
        }
    }
}
