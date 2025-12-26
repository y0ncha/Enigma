package enigma.engine;

import enigma.engine.exception.EngineException;
import enigma.engine.exception.MachineNotLoadedException;
import enigma.engine.exception.MachineNotConfiguredException;
import enigma.engine.factory.CodeFactoryImpl;
import enigma.engine.history.MachineHistory;
import enigma.engine.snapshot.EngineSnapshot;
import enigma.engine.snapshot.EngineSnapshotJson;
import enigma.loader.Loader;
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
 * Responsibilities:
 * <ul>
 *   <li>Load machine specifications from XML and keep the current {@link MachineSpec}.</li>
 *   <li>Validate and apply {@link CodeConfig} instances (manual or random) to the internal {@link Machine}.</li>
 *   <li>Process input strings through the configured {@link Machine} and return trace information.</li>
 *   <li>Maintain a small runtime history of configurations and processed messages.</li>
 * </ul>
 *
 * Threading/concurrency: callers are responsible for synchronization if the same EngineImpl
 * instance is accessed concurrently (methods are not synchronized internally).
 */
public class EngineImpl implements Engine {

    // ROTORS_IN_USE is now part of MachineSpec; the engine no longer declares a duplicate constant.

    private final Machine machine;
    private final Loader loader;
    private final CodeFactory codeFactory;
    private MachineHistory history;
    private boolean isSnapshot = false;
    private MachineSpec spec;
    private CodeState ogCodeState = enigma.shared.state.CodeState.notConfigured();
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
     * {@inheritDoc}
     */
    @Override
    public void loadMachine(String path) throws Exception {
        spec = loader.loadSpecs(path);
        this.history = new MachineHistory();
        this.ogCodeState = enigma.shared.state.CodeState.notConfigured();
        this.stringsProcessed = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MachineState machineData() {
        int rotors = spec == null ? 0 : spec.rotorsById().size();
        int reflectors = spec == null ? 0 : spec.reflectorsById().size();
        CodeState currCodeState = machine.getCodeState();
        return new MachineState(rotors, reflectors, stringsProcessed, this.ogCodeState, currCodeState );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configManual(CodeConfig config) {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        // validate config against spec
        EngineValidator.validateCodeConfig(spec, config);

        // create code and assign to machine
        Code code = codeFactory.create(spec, config);
        machine.setCode(code);

        // record config in history
        if (!isSnapshot){
            ogCodeState = machine.getCodeState();
        }
        history.recordConfig(ogCodeState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configRandom() {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        CodeConfig config = generateRandomConfig(spec);
        configManual(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessTrace process(String input) {

        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        if (!machine.isConfigured()) {
            throw new MachineNotConfiguredException("Machine is not configured");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        if (!machine.isConfigured()) {
            throw new MachineNotConfiguredException("Machine is not configured");
        }
        machine.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        // Nothing to clean up.
        // Included for interface completeness (console may call it before exiting).
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String history() {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        return history.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MachineSpec getMachineSpec() {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        return spec;
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

        // TODO add random plugs

        // rotorIds (left→right), positions as chars (left→right), reflectorId
        return new CodeConfig(chosenRotors, positions, reflectorId, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSnapshot(String basePath) {
        try{
            if (spec == null) {
                throw new EngineException(
                        "Cannot save snapshot: No machine specification loaded.");
            }
            MachineState state = machineData(); // uses ogCodeState, + stringsProcessed etc.
            EngineSnapshot snapshot = new EngineSnapshot(spec, state, history);
            EngineSnapshotJson.save(snapshot, basePath);
        }catch (Exception e){
            throw new EngineException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSnapshot(String basePath) {
        try {
            // 1) Load snapshot from JSON file
            EngineSnapshot snapshot = EngineSnapshotJson.load(basePath);
            // 2) Replace machine specification
            this.spec = snapshot.spec();
            // 3) Replace history
            this.history = snapshot.history() != null
                    ? snapshot.history()
                    : new MachineHistory();
            // 4) Restore machine runtime state
            MachineState state = snapshot.machineState();
            if (state != null) {
                this.stringsProcessed = state.stringsProcessed();
                this.ogCodeState = state.ogCodeState();
                CodeState theCurCodeState = state.curCodeState();
                boolean hasCurrent =
                        theCurCodeState != null &&
                                theCurCodeState != CodeState.NOT_CONFIGURED;
                if (hasCurrent) {
                    // Machine was configured when snapshot was taken
                    isSnapshot = true;
                    configManual(theCurCodeState.toConfig());
                    isSnapshot = false;
                }
            } else {
                // Defensive fallback in damaged snapshot file
                this.stringsProcessed = 0;
                this.ogCodeState = CodeState.NOT_CONFIGURED;
                machine.reset();
            }
        } catch (Exception e) {
            // Wrap everything in EngineException with meaningful message
            throw new EngineException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMachineDetails() {
        if (!machine.isConfigured()) {
            return "Machine not configured";
        }
        return machine.toString();
    }
}
