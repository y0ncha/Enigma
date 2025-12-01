package enigma.engine;

import enigma.engine.factory.CodeFactoryImpl;
import enigma.loader.Loader;
import enigma.loader.EnigmaLoadingException;
import enigma.loader.LoaderXml;
import enigma.engine.factory.CodeFactory;
import enigma.machine.MachineImpl;
import enigma.machine.code.Code;
import enigma.machine.Machine;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.security.SecureRandom;
import java.util.*;

/**
 * Default {@link Engine} implementation.
 *
 * <p>This class coordinates the high-level engine flow: it obtains a
 * {@link MachineSpec} from the configured {@link Loader}, generates or
 * validates {@link CodeConfig} instances, and delegates construction of
 * a runtime {@link Code} to a {@link CodeFactory} implementation. The
 * Engine assumes the {@link Loader} performs full validation of the
 * {@link MachineSpec} contents; the Engine validates runtime configuration
 * (code selection and positions) before creating a code instance.</p>
 *
 * <p>Responsibilities (concise):</p>
 * <ul>
 *   <li>Load machine specification (XML) via {@link Loader}.</li>
 *   <li>Produce validated {@link CodeConfig} (manual or random).</li>
 *   <li>Build a runtime {@link Code} using {@link CodeFactory} and assign it to
 *       the internal {@link Machine} instance.</li>
 * </ul>
 *
 * @since 1.0
 */
public class EngineImpl implements Engine {

    private static final int ROTORS_IN_USE = 3; // Can be dynamically configured in the future
    private final boolean DEBUG = false;

    private final Machine machine;
    private final Loader loader;
    private final CodeFactory codeFactory;

    private MachineSpec spec;
    private CodeConfig origConfig;

    /**
     * Construct an Engine that uses the default XML {@link Loader} and
     * the default {@link CodeFactory} implementation.
     *
     * <p>The engine creates a {@link Machine} instance and keeps it as an
     * internal field; the {@link Machine} is configured later when a
     * {@link Code} is created (manual or random).</p>
     */
    public EngineImpl() {
        this.machine = new MachineImpl();
        this.loader = new LoaderXml(ROTORS_IN_USE);
        this.codeFactory = new CodeFactoryImpl();
    }

    /**
     * Load a machine specification from the given path.
     *
     * <p>This method delegates actual XML parsing/validation to the configured
     * {@link Loader}. On success the loaded {@link MachineSpec} is stored in
     * the engine for subsequent code creation calls.</p>
     *
     * @param path filesystem path or resource identifier accepted by the loader
     * @throws RuntimeException when loading fails (wraps {@link EnigmaLoadingException})
     */
    @Override
    public void loadMachime(String path) {
        try {
            spec = loader.loadSpecs(path);
        }
        catch (EnigmaLoadingException e) {
            throw new RuntimeException("Failed to load machine XML: " + e.getMessage(), e);
        }
    }

    /**
     * Placeholder for feeding machine-level data into the engine.
     *
     * <p>Currently a no-op; kept for API completeness.</p>
     *
     * @param input arbitrary input string for future use
     */
    @Override
    public void machineData(String input) {
        // no-op for now
    }

    /**
     * Configure the machine using an explicit {@link CodeConfig} provided by
     * the caller.
     *
     * <p>The engine validates the configuration against the loaded
     * {@link MachineSpec} (uniqueness, existence of ids, and valid positions)
     * and uses {@link CodeFactory#create(MachineSpec, CodeConfig)} to obtain
     * a runtime {@link Code} instance. The created code is assigned to the
     * internal {@link Machine}.</p>
     *
     * @param config runtime code configuration (rotor ids, positions, reflector id)
     * @throws IllegalArgumentException when the configuration is invalid
     */
    @Override
    public void codeManual(CodeConfig config) {
        validateCodeConfig(spec, config);
        Code code = codeFactory.create(spec, config);
        if (origConfig == null) origConfig = config;
        machine.setCode(code);
    }

    /**
     * Create a random, valid {@link CodeConfig} and configure the machine
     * with the resulting code.
     *
     * <p>This method samples rotor ids, starting positions and a reflector
     * id using a local {@link SecureRandom} instance, validates the sampled
     * configuration and delegates to {@link #codeManual(CodeConfig)} so the
     * same creation path is used for manual and random flows.</p>
     *
     * @throws IllegalStateException when the machine spec is not loaded
     */
    public void codeRandom() {
        CodeConfig config = randomCodeConfig(spec);
        codeManual(config);
    }

    /**
     * Process an input string through the configured {@link Machine}.
     *
     * <p>The engine forwards characters one-by-one to {@link Machine#process}
     * and concatenates the results. {@link Machine} itself enforces that a
     * {@link Code} has been set and will throw if the machine is not ready.</p>
     *
     * @param input input string to process
     * @return processed output string
     * @throws IllegalStateException if the internal machine rejects processing
     */
    @Override
    public String process(String input) {
        StringBuilder output = new StringBuilder();
        for(char c : input.toCharArray()) {
            output.append(machine.process(c));
        }
        return output.toString();
    }

    /**
     * Produce runtime statistics for the engine.
     *
     * <p>Currently a placeholder; implementation may be added later.</p>
     */
    @Override
    public void statistics() {
        // no-op for now
    }

    // --- Flow helpers: machine creation and random code generation ---------

    /**
     * Generate a random {@link CodeConfig} from the loaded {@link MachineSpec}.
     *
     * <p>Sampling rules:
     * <ul>
     *   <li>Pick exactly {@link #ROTORS_IN_USE} unique rotor ids (left→right)</li>
     *   <li>Pick one reflector id at random</li>
     *   <li>Generate random starting positions (0 .. alphabet.size()-1) for each rotor</li>
     * </ul>
     * The returned {@link CodeConfig} uses left→right ordering for rotors and
     * positions (caller/engine conventions).</p>
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

        // Generate random starting positions (left → right)
        int alphaSize = spec.alphabet().size();
        List<Integer> positions = new ArrayList<>(ROTORS_IN_USE);
        for (int i = 0; i < ROTORS_IN_USE; i++) {
            positions.add(random.nextInt(alphaSize));
        }

        // Pick a random reflector
        List<String> reflectorIds = new ArrayList<>(spec.reflectorsById().keySet());
        String reflectorId = reflectorIds.get(random.nextInt(reflectorIds.size()));

        // rotorIds (left→right), positions (left→right), reflectorId
        return new CodeConfig(chosenRotors, positions, reflectorId);
    }

    // --- Engine-level validation helpers ------------------------------------------------

    /**
     * Minimal guard ensuring a non-null {@link MachineSpec} is present before
     * the engine uses it.
     *
     * <p>The loader is responsible for full spec validation (alphabet length,
     * mappings, notch sanity, etc.). The engine keeps a minimal presence check
     * to protect runtime flows.</p>
     *
     * @param spec machine specification instance
     * @throws IllegalArgumentException if {@code spec} is null
     */
    private void validateMachineSpec(MachineSpec spec) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
    }

    /**
     * Validate a {@link CodeConfig} against the (already-loaded) {@link MachineSpec}.
     *
     * <p>Checks performed here are limited to configuration-level constraints:
     * number of rotors/positions, uniqueness of rotor ids, existence of ids in
     * the spec, and valid numeric ranges for positions.</p>
     *
     * @param spec   machine specification (assumed valid by the loader)
     * @param config code configuration to validate
     * @throws IllegalArgumentException when validation fails
     */
    private void validateCodeConfig(MachineSpec spec, CodeConfig config) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        if (config == null) throw new IllegalArgumentException("CodeConfig must not be null");

        List<Integer> rotorIds = config.rotorIds();
        List<Integer> positions = config.initialPositions();
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

        int alphaSize = spec.alphabet().size();
        for (int i = 0; i < positions.size(); i++) {
            Integer p = positions.get(i);
            if (p == null || p < 0 || p >= alphaSize)
                throw new IllegalArgumentException("Invalid position at index " + i + ": " + p);
        }
    }
}
