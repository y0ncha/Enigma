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
     * {@inheritDoc}
     *
     * <p>High-level orchestration: load spec, validate, create machine,
     * generate random code config, validate config, build code and assign it.</p>
     */
    @Override
    public void loadXml(String path) {
        try {
            // 1. Load spec
            MachineSpec spec = loader.loadMachine(path);

            // 2. Validate spec
            validateMachineSpec(spec);

            // 3. Create machine (no validation here)
            Machine m = createMachine(spec);

            // 4. Generate random code configuration
            CodeConfig cfg = generateRandomCodeConfig(spec);

            // 5. Validate generated configuration
            validateCodeConfig(spec, cfg);

            // 6. Create Code via factory and assign
            Code code = codeFactory.create(spec, cfg);
            m.setCode(code);

            // 7. Store machine
            this.machine = m;
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
    public void codeManual() {
        Code code = null; // construct code from user input
        machine.setCode(code);
    }

    public void codeRandom() {
        // generate random code - not implemented; prefer loadXml path for now
        machine.setCode(null);
    }

    @Override
    public String process(String input) {
        if (machine == null) throw new IllegalStateException("Machine is not initialized");

        StringBuilder output = new StringBuilder();
        for(char c : input.toCharArray()) {
            output.append(machine.process(c));
        }
        return output.toString();
    }

    @Override
    public void statistics() {

    }

    // --- Flow helpers: machine creation and random code generation ---------

    /**
     * Create a runtime {@link Machine} for the provided specification.
     * No validation is performed here (caller must validate spec beforehand).
     *
     * @param spec validated machine spec
     * @return new Machine instance
     */
    private Machine createMachine(MachineSpec spec) {
        return new MachineImpl(new KeyboardImpl(spec.alphabet()));
    }

    /**
     * Generate a random {@link CodeConfig} from the machine specification.
     * This method performs only lightweight precondition checks (via
     * {@link #validateRandomCodePreconditions(MachineSpec, int)}) and returns
     * a sampled configuration; full validation is done by validateCodeConfig.
     *
     * @param spec machine specification
     * @return sampled CodeConfig (rotorIds left->right, positions left->right, reflectorId)
     */
    private CodeConfig generateRandomCodeConfig(MachineSpec spec) {
        final int REQUIRED_ROTORS = 3;
        validateRandomCodePreconditions(spec, REQUIRED_ROTORS);
        // Delegate to CodeFactoryImpl.createRandom to avoid duplication
        CodeFactory factory = new CodeFactoryImpl();
        return factory.createRandom(spec);
    }

    /**
     * Validate preconditions required for sampling a random code configuration.
     * Throws {@link IllegalArgumentException} when prerequisites are not met.
     *
     * @param spec machine specification
     * @param requiredRotors number of rotors required for sampling
     */
    private void validateRandomCodePreconditions(MachineSpec spec, int requiredRotors) {
        if (spec.rotorsById() == null) throw new IllegalArgumentException("MachineSpec rotors map must not be null");
        int available = spec.rotorsById().size();
        if (available < requiredRotors) {
            throw new IllegalArgumentException("Not enough rotors in spec to build machine: required " + requiredRotors + ", but got " + available);
        }
        if (spec.reflectorsById() == null || spec.reflectorsById().isEmpty())
            throw new IllegalArgumentException("MachineSpec must define at least one reflector to sample randomly");
    }

    // --- Engine-level validation helpers ------------------------------------------------

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
