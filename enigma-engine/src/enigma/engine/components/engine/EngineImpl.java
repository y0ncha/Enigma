package enigma.engine.components.engine;

import enigma.engine.components.loader.Loader;
import enigma.engine.components.loader.EnigmaLoadingException;
import enigma.engine.components.loader.LoaderXml;
import enigma.engine.components.model.MachineSpec;
import enigma.engine.components.factory.CodeFactory;
import enigma.engine.components.factory.CodeFactoryImpl;
import enigma.engine.components.dto.CodeConfig;
import enigma.machine.component.code.Code;
import enigma.machine.component.machine.Machine;
import enigma.machine.component.machine.MachineImpl;
import enigma.machine.component.keyboard.KeyboardImpl;

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

    private Machine machine;
    private final Loader loader;

    private final CodeFactory codeFactory;

    /**
     * Construct an Engine that uses the default XML loader and code factory.
     */
    public EngineImpl() {
        this.loader = new LoaderXml();
        this.codeFactory = new CodeFactoryImpl();
    }

    /**
     * Load machine specification from an XML file, validate it, create a
     * runtime {@link Machine} and apply a sampled, validated {@link Code}.
     *
     * <p>The method performs the following high-level steps:
     * <ol>
     *   <li>Load the specification using the configured {@link Loader}.</li>
     *   <li>Validate the specification at the engine boundary.</li>
     *   <li>Sample a random, valid {@link CodeConfig} (rotors, positions, reflector).</li>
     *   <li>Validate the generated configuration and delegate to the factory to
     *       construct the {@link Code} instance.</li>
     * </ol>
     *
     * @param path file-system path to the machine XML
     * @throws RuntimeException wrapping {@link EnigmaLoadingException} when loading fails
     * @throws IllegalArgumentException when the spec or generated config is invalid
     */
    @Override
    public void loadXml(String path) {
        try {
            MachineSpec spec = loader.loadMachine(path);

            // validate the spec at engine boundary (now engine owns validation)
            validateMachineSpec(spec);

            // create machine with keyboard configured for this alphabet
            Machine m = new MachineImpl(new KeyboardImpl(spec.alphabet()));

            // generate random CodeConfig here (engine responsibility)
            SecureRandom rnd = new SecureRandom();

            // ensure enough rotors
            int available = (spec.rotorsById() == null) ? 0 : spec.rotorsById().size();
            if (available < 3) {
                throw new IllegalArgumentException("Not enough rotors in spec to build machine");
            }

            // sample 3 rotor ids randomly (left->right order)
            List<Integer> pool = new ArrayList<>(spec.rotorsById().keySet());
            Collections.shuffle(pool, rnd);
            List<Integer> chosenRotors = new ArrayList<>(pool.subList(0, 3));

            // sample starting positions left->right
            List<Integer> positions = new ArrayList<>();
            int alphaSize = spec.alphabet().size();
            for (int i = 0; i < 3; i++) positions.add(rnd.nextInt(alphaSize));

            // pick random reflector
            List<String> reflectors = new ArrayList<>(spec.reflectorsById().keySet());
            String reflectorId = reflectors.get(rnd.nextInt(reflectors.size()));

            CodeConfig cfg = new CodeConfig(chosenRotors, positions, reflectorId);

            // validate the generated config using engine validation
            validateCodeConfig(spec, cfg);

            // build code via factory (factory assumes inputs are valid)
            Code code = codeFactory.create(spec, cfg);
            m.setCode(code);

            this.machine = m;
        }
        catch (EnigmaLoadingException e) {
            throw new RuntimeException("Failed to load machine XML: " + e.getMessage(), e);
        }
    }

    /**
     * Accept arbitrary machine-related input. Implementation is currently a
     * no-op and reserved for future interactive or scripted flows.
     *
     * @param input free-form input used to update engine state
     */
    @Override
    public void machineData(String input) {
        // no-op for now
    }

    /**
     * Switch to manual configuration mode. Implementations should collect
     * manual rotor/reflector/position values and apply them to the machine.
     * This implementation leaves the behavior to the caller.
     */
    @Override
    public void codeManual() {
        Code code = null; // construct code from user input
        machine.setCode(code);
    }

    /**
     * Assign a randomly generated code to the current machine. Prefer the
     * `loadXml` flow for deterministic sampling + validation; this method
     * currently delegates to factory helpers or may be implemented later.
     */
    public void codeRandom() {
        // generate random code - not implemented; prefer loadXml path for now
        machine.setCode(null);
    }

    /**
     * Process text through the configured machine/code and return the result.
     *
     * @param input input text to process
     * @return processed output
     * @throws IllegalStateException if the machine is not initialized
     */
    @Override
    public String process(String input) {
        if (machine == null) throw new IllegalStateException("Machine is not initialized");

        StringBuilder output = new StringBuilder();
        for(char c : input.toCharArray()) {
            output.append(machine.process(c));
        }
        return output.toString();
    }

    /**
     * Emit runtime statistics. The current implementation is a placeholder.
     */
    @Override
    public void statistics() {

    }

    // --- Engine-level validation helpers (moved from ValidationService) ---

    /**
     * Validate the loaded {@link MachineSpec} for internal consistency.
     *
     * <p>Checks include: alphabet presence, rotor/reflector map existence,
     * mapping lengths, notch range and reflector symmetry.</p>
     *
     * @param spec machine specification to validate
     * @throws IllegalArgumentException when validation fails
     */
    private void validateMachineSpec(MachineSpec spec) {
        if (spec == null) throw new IllegalArgumentException("MachineSpec must not be null");
        if (spec.alphabet() == null) throw new IllegalArgumentException("MachineSpec alphabet must not be null");
        if (spec.rotorsById() == null || spec.rotorsById().isEmpty())
            throw new IllegalArgumentException("MachineSpec must define at least one rotor");
        if (spec.reflectorsById() == null || spec.reflectorsById().isEmpty())
            throw new IllegalArgumentException("MachineSpec must define at least one reflector");

        int alphaSize = spec.alphabet().size();

        // Basic rotor checks
        for (var e : spec.rotorsById().entrySet()) {
            Integer id = e.getKey();
            var rs = e.getValue();
            if (rs == null) throw new IllegalArgumentException("Rotor spec is null for id: " + id);
            int[] f = rs.getForwardMapping();
            int[] b = rs.getBackwardMapping();
            if (f == null || b == null)
                throw new IllegalArgumentException("Rotor " + id + " mappings must not be null");
            if (f.length != alphaSize || b.length != alphaSize)
                throw new IllegalArgumentException("Rotor " + id + " mapping length must equal alphabet size (" + alphaSize + ")");
            if (rs.getNotchIndex() < 0 || rs.getNotchIndex() >= alphaSize)
                throw new IllegalArgumentException("Rotor " + id + " notchIndex out of range");
        }

        // Basic reflector checks
        for (var e : spec.reflectorsById().entrySet()) {
            String rid = e.getKey();
            var r = e.getValue();
            if (r == null) throw new IllegalArgumentException("Reflector spec is null for id: " + rid);
            int[] mapping = r.getMapping();
            if (mapping == null || mapping.length != alphaSize)
                throw new IllegalArgumentException("Reflector " + rid + " mapping must be length " + alphaSize);
            for (int i = 0; i < mapping.length; i++) {
                int j = mapping[i];
                if (j < 0 || j >= alphaSize)
                    throw new IllegalArgumentException("Reflector " + rid + " mapping out of range: " + i + "->" + j);
                if (j == i) throw new IllegalArgumentException("Reflector " + rid + " maps index to itself at " + i);
                if (mapping[j] != i) throw new IllegalArgumentException("Reflector " + rid + " mapping is not symmetric at " + i + "<->" + j);
            }
        }
    }

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

        if (rotorIds.size() != 3) throw new IllegalArgumentException("Exactly 3 rotors must be selected");
        if (positions.size() != 3) throw new IllegalArgumentException("Exactly 3 initial positions must be provided");

        if (spec.rotorsById() == null || spec.rotorsById().size() < 3)
            throw new IllegalArgumentException("MachineSpec does not contain enough rotors");

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
            if (p == null) throw new IllegalArgumentException("Position at index " + i + " is null");
            if (p < 0 || p >= alphaSize) throw new IllegalArgumentException("Invalid position index at " + i + ": " + p);
        }
    }
}
