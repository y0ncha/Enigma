package enigma.engine.factory;

import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;
import enigma.shared.spec.ReflectorSpec;
import enigma.shared.spec.RotorSpec;

import enigma.engine.EngineImpl;
import enigma.machine.alphabet.Alphabet;
import enigma.machine.code.Code;
import enigma.machine.code.CodeImpl;
import enigma.machine.rotor.Rotor;
import enigma.machine.reflector.Reflector;

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
 *       {@link EngineImpl}).</li>
 * </ul>
 *
 * Thread-safety: instances of this class are stateless and thread-safe.
 *
 * @since 1.0
 */
public class CodeFactoryImpl implements CodeFactory {

    /**
     * Create a {@link Code} instance from a validated {@link MachineSpec} and
     * {@link CodeConfig}.
     * Preconditions (caller responsibility):
     * <ul>
     *   <li>{@code spec} and {@code config} must not be null.</li>
     *   <li>{@code config.rotorIds()} and {@code config.initialPositions()} must
     *       contain exactly {@code DEFAULT_ROTOR_COUNT} entries (left→right order).</li>
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
                alphabet,
                rotors,
                reflector,
                rotorIdsForCode,
                new ArrayList<>(positions),
                config.reflectorId()
        );
    }
}
