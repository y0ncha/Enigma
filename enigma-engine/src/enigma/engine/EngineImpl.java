package enigma.engine;

import enigma.engine.exception.EngineException;
import enigma.engine.exception.MachineNotLoadedException;
import enigma.engine.exception.MachineNotConfiguredException;
import enigma.engine.factory.CodeFactoryImpl;
import enigma.engine.history.MachineHistory;
import enigma.loader.Loader;
import enigma.loader.exception.EnigmaLoadingException;
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
 * One-line: load machine specs, validate configs and run the machine.
 */
public class EngineImpl implements Engine {

    // ROTORS_IN_USE is now part of MachineSpec; the engine no longer declares a duplicate constant.

    private final Machine machine;
    private final Loader loader;
    private final CodeFactory codeFactory;
    private MachineHistory history;

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
        this.loader = new LoaderXml();
        this.codeFactory = new CodeFactoryImpl();
        this.history = new MachineHistory();
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
     * @throws EngineException if parsing or validation fails (wraps EnigmaLoadingException with context)
     */
    @Override
    public void loadMachine(String path) {
        try {
            spec = loader.loadSpecs(path);
            this.history = new MachineHistory();
            this.ogCodeState = null;
            this.stringsProcessed = 0;
        }
        catch (EnigmaLoadingException e) {
            throw new EngineException(
                String.format(
                    "Failed to load machine specification from XML file: %s. " +
                    "Error: %s. " +
                    "Fix: Ensure the XML file exists, is well-formed, and satisfies all validation rules.",
                    path, e.getMessage()),
                e);
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
     * @throws enigma.engine.exception.InvalidConfigurationException if validation fails
     * @throws MachineNotLoadedException if spec is not loaded
     */
    @Override
    public void configManual(CodeConfig config) {
        if (spec == null) {
            throw new MachineNotLoadedException(
                "Cannot configure machine: No machine specification loaded. " +
                "Fix: Load a machine specification using loadMachine(path) before configuring.");
        }
        // validate config against spec
        EngineValidator.validateCodeConfig(spec, config);

        // create code and assign to machine
        Code code = codeFactory.create(spec, config);
        machine.setCode(code);

        // record config in history
        ogCodeState = machine.getCodeState();
        history.recordConfig(ogCodeState);
    }


    /**
     * Generate a random, valid {@link CodeConfig} and configure the machine.
     *
     * <p>Sampling strategy:</p>
     * <ul>
     *   <li>Pick the number of rotors indicated by the loaded {@link enigma.shared.spec.MachineSpec#getRotorsInUse()} (left→right)</li>
     *   <li>Generate random char positions (left→right) from alphabet</li>
     *   <li>Pick one random reflector ID</li>
     * </ul>
     * <p>Delegates to {@link #configManual(CodeConfig)} for validation and construction.</p>
     *
     * @throws MachineNotLoadedException when spec is not loaded
     */
    @Override
    public void configRandom() {
        if (spec == null) {
            throw new MachineNotLoadedException(
                "Cannot generate random configuration: No machine specification loaded. " +
                "Fix: Load a machine specification using loadMachine(path) before generating random configuration.");
        }
        CodeConfig config = generateRandomConfig(spec);
        configManual(config);
    }

    /**
     * Process the provided input string through the currently configured
     * machine/code with detailed debugging information.
     *
     * @param input the input text to process
     * @return detailed debug trace of the processing steps
     * @throws MachineNotLoadedException if machine specification is not loaded
     * @throws MachineNotConfiguredException if machine is not configured
     * @throws enigma.engine.exception.InvalidMessageException if input is null or contains invalid characters
     */
    @Override
    public ProcessTrace process(String input) {
        // Validate machine is loaded
        if (spec == null) {
            throw new MachineNotLoadedException(
                "Cannot process message: No machine specification loaded. " +
                "Fix: Load a machine specification using loadMachine(path) before processing messages.");
        }
        
        // Validate machine is configured
        if (!machine.isConfigured()) {
            throw new MachineNotConfiguredException(
                "Cannot process message: Machine is not configured. " +
                "Fix: Configure the machine using configManual(config) or configRandom() before processing messages.");
        }
        
        // Validate input is not null and contains only valid characters
        EngineValidator.validateInputInAlphabet(spec, input);

        List<SignalTrace> traces = new ArrayList<>();
        StringBuilder output = new StringBuilder();

        long start = System.nanoTime();
        for (char c : input.toCharArray()) {
            SignalTrace trace = machine.process(c);
            traces.add(trace);
            output.append(trace.outputChar());
        }
        long duration = System.nanoTime() - start;

        this.stringsProcessed++;

        // Record processed message with duration in nanoseconds
        history.recordMessage(input, output.toString(), duration);

        return new ProcessTrace(output.toString(), List.copyOf(traces));
    }

    @Override
    public void reset() {
        if (spec == null) {
            throw new MachineNotLoadedException(
                    "Cannot reset machine: No machine specification loaded. " +
                            "Fix: Load a machine specification using loadMachine(path) before resetting.");
        }

        if (!machine.isConfigured()) {
            throw new MachineNotConfiguredException(
                    "Cannot reset machine: Machine is not configured. " +
                            "Fix: Configure the machine using configManual(config) or configRandom() before resetting.");
        }
        machine.reset();
    }

    @Override
    public void terminate() {
        // Nothing to clean up.
        // Included for interface completeness (console may call it before exiting).
    }

    @Override
    public String history() {
        return history.toString();
    }

    @Override
    @Deprecated
    public MachineSpec getMachineSpec() {
        return spec;
    }

    @Override
    @Deprecated
    public CodeConfig getCurrentCodeConfig() {
        if (!machine.isConfigured()) {
            return null;
        }
        return machine.getConfig();
    }

    @Override
    @Deprecated
    public long getTotalProcessedMessages() {
        return stringsProcessed;
        // TODO Yonatan - deprecate : machineData instead
    }

    // ---------------------------------------------------------
    // Flow helpers: machine creation and random code generation
    // ---------------------------------------------------------

    /**
     * Generate a random {@link CodeConfig} from the loaded {@link MachineSpec}.
     *
     * <p>Sampling rules:</p>
     * <ul>
     *   <li>Pick exactly the number of rotors specified by {@link enigma.shared.spec.MachineSpec#getRotorsInUse()} (left→right)</li>
     *   <li>Pick one reflector ID at random</li>
     *   <li>Generate random starting positions as chars (left→right)</li>
     * </ul>
     * <p>The returned {@link CodeConfig} uses left→right ordering for rotors and
     * positions (user-facing conventions).</p>
     *
     * @param spec machine specification (must be non-null)
     * @return randomly sampled {@link CodeConfig}
     */
    private CodeConfig generateRandomConfig(MachineSpec spec) {
        SecureRandom random = new SecureRandom();

        // Shuffle available rotor IDs and pick exactly ROTORS_IN_USE of them in random order (left → right)
        int needed = spec.getRotorsInUse();
        List<Integer> rotorPool = new ArrayList<>(spec.rotorsById().keySet());
        Collections.shuffle(rotorPool, random);
        List<Integer> chosenRotors = new ArrayList<>(rotorPool.subList(0, needed)); // left→right

        // Generate random starting positions as characters (left → right)
        int alphaSize = spec.alphabet().size();
        List<Character> positions = new ArrayList<>(needed);
        for (int i = 0; i < needed; i++) {
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
}
