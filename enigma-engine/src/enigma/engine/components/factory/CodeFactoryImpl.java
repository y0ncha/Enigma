package enigma.engine.components.factory;

import enigma.engine.components.dto.CodeConfig;
import enigma.engine.components.model.MachineSpec;
import enigma.engine.components.model.RotorSpec;
import enigma.engine.components.model.ReflectorSpec;

import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.code.Code;
import enigma.machine.component.code.CodeImpl;
import enigma.machine.component.rotor.Rotor;
import enigma.machine.component.reflector.Reflector;

import java.security.SecureRandom;
import java.util.*;

/**
 * Default implementation of {@link CodeFactory}.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Create a {@link Code} instance from a {@link MachineSpec} and a
 *       {@link CodeConfig} (the configuration describes which rotor ids and
 *       initial positions to use).</li>
 *   <li>Provide a convenience method to build a random, valid {@link Code}
 *       when given a valid {@link MachineSpec}.</li>
 * </ul>
 * </p>
 *
 * Ordering conventions (important):
 * <ul>
 *   <li>All {@code CodeConfig} inputs are expected in left→right order
 *       (user-facing ordering).</li>
 *   <li>Runtime {@link Code} and component lists are built in right→left order
 *       (the order rotors are processed by the machine). The factory converts
 *       the ordering as needed.</li>
 * </ul>
 *
 * Validation and error handling:
 * <ul>
 *   <li>Factory methods assume inputs are validated by the caller (for example,
 *       {@link enigma.engine.components.engine.EngineImpl}). {@link #createRandom(MachineSpec)}
 *       expects the engine to perform validation on the generated configuration
 *       before constructing the {@link Code}.</li>
 *   <li>No explicit validation is performed in this class; callers must ensure
 *       preconditions are met before invoking factory methods.</li>
 * </ul>
 *
 * Thread-safety: instances of this class are stateless and thread-safe; the
 * {@link #createRandom(MachineSpec)} method creates its own local
 * {@link SecureRandom} instance for sampling.
 *
 * @since 1.0
 */
public class CodeFactoryImpl implements CodeFactory {

    private static final int DEFAULT_ROTOR_COUNT = 3; // number of rotors to assemble

    /**
     * Create a new CodeFactoryImpl instance.
     */
    public CodeFactoryImpl() {
    }

    /**
     * Create a {@link Code} instance from a validated {@link MachineSpec} and
     * {@link CodeConfig}.
     * Preconditions (caller responsibility):
     * <ul>
     *   <li>{@code spec} and {@code config} must not be null.</li>
     *   <li>{@code config.rotorIds()} and {@code config.initialPositions()} must
     *       contain exactly {@code DEFAULT_ROTOR_COUNT} entries (left→right order).
     *   <li>{@code config.reflectorId()} must be non-empty and refer to a valid
     *       reflector id defined in {@code spec}.</li>
     * </ul>
     *
     * Behavior notes:
     * <ul>
     *   <li>The factory constructs per-alphabet sub-factories ({@link RotorFactoryImpl}
     *       and {@link ReflectorFactoryImpl}) to build concrete rotor/reflector
     *       instances.</li>
     *   <li>The provided positions (left→right) are reversed before constructing
     *       the runtime rotors so the resulting {@link Code} contains runtime
     *       metadata in right→left order.</li>
     * </ul>
     *
     * @param spec validated machine specification (alphabet, rotor/reflector specs)
     * @param config validated code configuration (left→right ordering)
     * @return a newly created immutable {@link Code} instance
     */
    @Override
    public Code create(MachineSpec spec, CodeConfig config) {
        // Assume inputs (spec, config) are already validated by caller

        Alphabet alphabet = spec.alphabet();
        RotorFactory rotorFactory = new RotorFactoryImpl(alphabet);
        ReflectorFactory reflectorFactory = new ReflectorFactoryImpl(alphabet);

        // Positions are provided as numeric indices left->right, reverse them to right->left
        List<Integer> positions = new ArrayList<>(config.initialPositions());
        Collections.reverse(positions);

        // Rotors
        List<Rotor> rotors = new ArrayList<>();
        List<Integer> rotorIds = new ArrayList<>(config.rotorIds());
        Collections.reverse(rotorIds);

        for (int i = 0; i < rotorIds.size(); i++) { // Create only the requested rotors
            int rotorId = rotorIds.get(i);
            RotorSpec rs = spec.getRotorById(rotorId);
            int startPosition = positions.get(i);
            rotors.add(rotorFactory.create(rs, startPosition));
        }

        // Reflector
        ReflectorSpec rf = spec.getReflectorById(config.reflectorId());
        Reflector reflector = reflectorFactory.create(rf);

        // rotorIds for CodeImpl should be in right→left order (we reversed above)
        List<Integer> rotorIdsForCode = new ArrayList<>(rotorIds);

        return new CodeImpl(
                rotors,
                reflector,
                rotorIdsForCode,
                new ArrayList<>(positions),
                config.reflectorId()
        );
    }

    /**
     * Create a random, valid {@link Code} using the provided {@link MachineSpec}.
     * This method samples exactly {@code DEFAULT_ROTOR_COUNT} unique rotor ids,
     * picks a random reflector, and generates matching random start positions
     * (all in left→right order). The generated {@link CodeConfig} should be
     * validated by the caller (usually the Engine) before constructing the final
     * {@link Code}.
     *
     * @param spec machine specification; must contain at least {@code DEFAULT_ROTOR_COUNT}
     *             rotors and at least one reflector
     * @return created {@link Code}
     */
    @Override
    public Code createRandom(MachineSpec spec) {
        // Assume caller already validates spec

        SecureRandom random = new SecureRandom();

        Alphabet alphabet = spec.alphabet();

        // Shuffle available rotor IDs and pick exactly DEFAULT_ROTOR_COUNT of them in random order
        List<Integer> pool = new ArrayList<>(spec.rotorsById().keySet());
        Collections.shuffle(pool, random);
        List<Integer> chosenRotors = new ArrayList<>(pool.subList(0, DEFAULT_ROTOR_COUNT)); // left->right order (randomized)

        // Generate random starting positions (left->right)
        List<Integer> positions = new ArrayList<>(DEFAULT_ROTOR_COUNT);
        int alphaSize = alphabet.size();
        for (int i = 0; i < DEFAULT_ROTOR_COUNT; i++) {
            positions.add(random.nextInt(alphaSize));
        }

        // Pick a random reflector
        List<String> reflectors = new ArrayList<>(spec.reflectorsById().keySet());
        String reflectorId = reflectors.get(random.nextInt(reflectors.size()));

        CodeConfig cfg = new CodeConfig(
                chosenRotors,
                positions,
                reflectorId
        );

        // invoking create(..) with the auto generated config
        return create(spec, cfg);
    }
}
