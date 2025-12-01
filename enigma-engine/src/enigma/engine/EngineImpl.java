package enigma.engine;

import enigma.engine.factory.CodeFactoryImpl;
import enigma.loader.Loader;
import enigma.loader.EnigmaLoadingException;
import enigma.loader.LoaderXml;
import enigma.engine.factory.CodeFactory;
import enigma.machine.MachineImpl;
import enigma.machine.code.Code;
import enigma.machine.Machine;
import enigma.shared.dto.CodeConfig;
import enigma.shared.spec.MachineSpec;

import java.security.SecureRandom;
import java.util.*;

/**
 * Default {@link Engine} implementation.
 *
 * <p>This class is responsible for loading a machine specification from XML,
 * validating the specification and configurations, creating a {@link Machine}
 * and wiring a runtime {@link Code} using {@link CodeFactory}.</p>
 *
 * <p>Validation is performed at the engine boundary (see private helpers)
 * and factories are expected to construct runtime objects from validated
 * inputs.</p>
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
     * Construct an Engine that uses the default XML loader and code factory.
     */
    public EngineImpl() {
        this.machine = new MachineImpl();
        this.loader = new LoaderXml(ROTORS_IN_USE);
        this.codeFactory = new CodeFactoryImpl();
    }

    /**
     * {@inheritDoc}
     *
     * <p>High-level orchestration: load spec, validate, create machine,
     * generate random code config, validate config, build code and assign it.</p>
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

    @Override
    public void machineData(String input) {
        // no-op for now
    }

    @Override
    public void codeManual(CodeConfig config) {
        validateCodeConfig(spec, config);
        Code code = codeFactory.create(spec, config);
        if (origConfig == null) origConfig = config;
        machine.setCode(code);
    }

    public void codeRandom() {
        CodeConfig config = generateRandomCodeConfig(spec);
        codeManual(config);
    }

    @Override
    public String process(String input) {
        StringBuilder output = new StringBuilder();
        for(char c : input.toCharArray()) {
            output.append(machine.process(c));
        }
        return output.toString();
    }

    @Override
    public void statistics() {
        // no-op for now
    }

    // --- Flow helpers: machine creation and random code generation ---------

    /**
     * Generate a random {@link CodeConfig} from the machine specification.
     * This method performs only lightweight precondition checks (via
     * a sampled configuration; full validation is done by validateCodeConfig.
     *
     * @param spec machine specification
     * @return sampled CodeConfig (rotorIds left->right, positions left->right, reflectorId)
     */
    private CodeConfig generateRandomCodeConfig(MachineSpec spec) {
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
     * Validate a {@link CodeConfig} against a {@link MachineSpec}.
     *
     * <p>Checks include: non-null fields, exact rotor count (3), uniqueness,
     * existence in the spec and positions range.</p>
     *
     * @param spec   machine specification
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
