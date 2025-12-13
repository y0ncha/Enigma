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
 * Coordinates machine loading, validation, and message processing.
 *
 * <p>Manages machine specification, configuration, history tracking,
 * and delegates to machine components for actual encryption.</p>
 *
 * <p>Not thread-safe. Callers must synchronize concurrent access.</p>
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
     * Create an engine with default loader and factory implementations.
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
        this.curCodeState = enigma.shared.state.CodeState.notConfigured();
        this.stringsProcessed = 0;
    }

    /**
     * Return a snapshot of machine metadata and current code states.
     *
     * <p>The returned {@link MachineState} contains:
     * <ul>
     *   <li>Number of rotors available in the spec</li>
     *   <li>Number of reflectors available in the spec</li>
     *   <li>Number of strings processed so far</li>
     *   <li>The original configured code state (if configured)</li>
     *   <li>The current runtime code state (may change as the machine processes input)</li>
     * </ul>
     *
     * @return a {@link MachineState} snapshot; fields may be null if not applicable (e.g. no spec loaded)
     */
    @Override
    public MachineState machineData() {
        int rotors = spec == null ? 0 : spec.rotorsById().size();
        int reflectors = spec == null ? 0 : spec.reflectorsById().size();
        CodeState currCodeState = machine.getCodeState();
        return new MachineState(rotors, reflectors, stringsProcessed, this.curCodeState, currCodeState );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configManual(CodeConfig config) {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        EngineValidator.validateCodeConfig(spec, config);

        Code code = codeFactory.create(spec, config);
        machine.setCode(code);

        if (!isSnapshot){
            curCodeState = machine.getCodeState();
        }
        history.recordConfig(curCodeState);
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
     * Reset the currently-configured machine to its initial state for the current code.
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
     * Terminate the engine and release any resources. This implementation currently
     * has no resources to release; method exists for interface completeness.
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
     * Deprecated accessor for the loaded MachineSpec. Prefer using EngineState APIs.
     *
     * @return the currently loaded {@link MachineSpec}, or null if none loaded
     * @deprecated kept for backward compatibility with older tests/console
     */
    @Override
    @Deprecated
    public MachineSpec getMachineSpec() {
        if (spec == null) {
            throw new MachineNotLoadedException("No machine loaded");
        }
        return spec;
    }

    /**
     * Deprecated convenience to expose the current {@link CodeConfig} from the machine.
     *
     * @return the currently-configured {@link CodeConfig} or null if the machine is not configured
     * @deprecated use history or machine state inspection APIs instead
     */
    @Override
    @Deprecated
    public CodeConfig getCurrentCodeConfig() {
        if (!machine.isConfigured()) {
            return null;
        }
        return machine.getConfig();
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

    /**
     * Bonus: Save the current machine state (spec + code + history + statistics)
     * into a JSON snapshot file.
     *
     * @param basePath full path without extension (e.g. "C:\\tmp\\my-machine")
     * @throws EngineException if no spec is loaded or snapshot saving fails
     */
    @Override
    public void saveSnapshot(String basePath) {
        try{
            if (spec == null) {
                throw new EngineException("Machine specification is not loaded");
            }
            MachineState state = machineData();
            EngineSnapshot snapshot = new EngineSnapshot(spec, state, history);
            EngineSnapshotJson.save(snapshot, basePath);
        }catch (Exception e){
            throw new EngineException(e.getMessage(), e);
        }
    }

    /**
     * Bonus: Load a machine state from a previously saved JSON snapshot file.
     *
     * <p>This method REPLACES the current machine specification, history and
     * counters with the loaded data.</p>
     *
     * @param basePath full path without extension (e.g. "C:\\tmp\\my-machine")
     * @throws EngineException if loading fails or snapshot is invalid
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
                this.curCodeState = state.ogCodeState();
                CodeState theCurCodeState = state.curCodeState();
                boolean hasCurrent =
                        theCurCodeState != null &&
                                theCurCodeState != CodeState.NOT_CONFIGURED;
                if (hasCurrent) {
                    // Machine was configured when snapshot was taken
                    isSnapshot = true;
                    configManual(theCurCodeState.toCodeConfig());
                    isSnapshot = false;
                }
            } else {
                // Defensive fallback in damaged snapshot file
                this.stringsProcessed = 0;
                this.curCodeState = CodeState.NOT_CONFIGURED;
                machine.reset();
            }
        } catch (Exception e) {
            // Wrap everything in EngineException with meaningful message
            throw new EngineException(e.getMessage());
        }
    }
}
