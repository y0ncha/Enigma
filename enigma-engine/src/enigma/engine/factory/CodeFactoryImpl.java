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
 * <p><b>Ordering conventions (important):</b></p>
 * <ul>
 *   <li>All {@link CodeConfig} inputs (rotor ids and initial positions)
 *       are given in <b>left→right</b> order, exactly as they appear in the
 *       machine window and in the XML definition.</li>
 *
 *   <li>The factory <b>preserves left→right ordering</b>. It does not reverse
 *       rotor ids or positions.</li>
 *
 *   <li>The runtime {@link Code} object also stores rotors in left→right
 *       order. The {@code Machine} is responsible for iterating rotors in
 *       the correct physical direction during processing:
 *       <ul>
 *         <li>Forward signal: right→left (iterate high index → 0)</li>
 *         <li>Backward signal: left→right (iterate 0 → high index)</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p>This design eliminates the previous right→left storage convention,
 * aligns all internal structures with the XML/user-facing representation,
 * and avoids unnecessary list reversals or mental conversion.</p>
 *
 * <p>The factory assumes that all validation is performed by the caller
 * (typically {@link EngineImpl}).</p>
 *
 * @since 1.0
 */
public class CodeFactoryImpl implements CodeFactory {

    /**
     * Create a {@link Code} instance from a validated {@link MachineSpec}
     * and {@link CodeConfig}.
     *
     * <p><b>Input ordering:</b> rotor ids and initial positions must be
     * provided in <b>left→right</b> order. For example, {@code [3,2,1]}
     * means:
     * <ul>
     *   <li>index 0 → left rotor (id=3)</li>
     *   <li>index 1 → middle rotor (id=2)</li>
     *   <li>index 2 → right rotor (id=1)</li>
     * </ul>
     * </p>
     *
     * <p><b>Runtime behavior:</b></p>
     * <ul>
     *   <li>This factory preserves the left→right ordering.</li>
     *   <li>No reversal is performed.</li>
     *   <li>The resulting {@link Code} exposes rotors in left→right order.</li>
     *   <li>The {@code MachineImpl} handles right→left / left→right traversal
     *       during forward/backward signal propagation.</li>
     * </ul>
     *
     * <p><b>Validation:</b> The method assumes that:
     * <ul>
     *   <li>{@code spec} and {@code config} are not null.</li>
     *   <li>{@code config.rotorIds()} and {@code config.initialPositions()}
     *       contain exactly the required number of rotors (usually 3).</li>
     *   <li>{@code config.reflectorId()} refers to a valid reflector in the spec.</li>
     * </ul>
     * </p>
     *
     * @param spec   validated machine specification
     * @param config validated code configuration in left→right order
     * @return an immutable {@link Code} instance containing rotors, positions,
     *         and reflector stored in left→right order
     */
    @Override
    public Code createVirtual(MachineSpec spec, CodeConfig config) {
        // Assume inputs (spec, config) are already validated by caller

        Alphabet alphabet = spec.alphabet();
        RotorFactory rotorFactory = new RotorFactoryImpl(alphabet);
        ReflectorFactory reflectorFactory = new ReflectorFactoryImpl(alphabet);

        // Positions: left -> right, as given in config
        List<Integer> positionsLeftToRight = new ArrayList<>(config.initialPositions());

        // Rotors: build in left -> right order
        List<Rotor> rotorsLeftToRight = new ArrayList<>();
        List<Integer> rotorIdsLeftToRight = new ArrayList<>(config.rotorIds());

        for (int i = 0; i < rotorIdsLeftToRight.size(); i++) {
            int rotorId = rotorIdsLeftToRight.get(i);   // left -> right
            RotorSpec rs = spec.getRotorById(rotorId);
            int startPosition = positionsLeftToRight.get(i); // same index, left -> right
            Rotor rotor = rotorFactory.createVirtual(rs, startPosition);
            rotorsLeftToRight.add(rotor);
        }

        // Reflector
        ReflectorSpec rf = spec.getReflectorById(config.reflectorId());
        Reflector reflector = reflectorFactory.create(rf);

        // CodeImpl now receives everything in left -> right order
        return new CodeImpl(
                alphabet,
                rotorsLeftToRight,                 // left -> right
                reflector,
                rotorIdsLeftToRight,              // left -> right
                positionsLeftToRight,             // left -> right
                config.reflectorId()
        );
    }

    @Override
    public Code createMechanical(MachineSpec spec, CodeConfig config) {
        // Assume inputs (spec, config) are already validated by caller

        Alphabet alphabet = spec.alphabet();
        RotorFactory rotorFactory = new RotorFactoryImpl(alphabet);
        ReflectorFactory reflectorFactory = new ReflectorFactoryImpl(alphabet);

        // Positions: left -> right, as given in config
        List<Integer> positionsLeftToRight = new ArrayList<>(config.initialPositions());

        // Rotors: build in left -> right order
        List<Rotor> rotorsLeftToRight = new ArrayList<>();
        List<Integer> rotorIdsLeftToRight = new ArrayList<>(config.rotorIds());

        for (int i = 0; i < rotorIdsLeftToRight.size(); i++) {
            int rotorId = rotorIdsLeftToRight.get(i);          // left -> right
            RotorSpec rs = spec.getRotorById(rotorId);
            int targetPosition = positionsLeftToRight.get(i);  // desired window index (0..N-1)

            // Create mechanical rotor in some base orientation (e.g. 0)
            Rotor rotor = rotorFactory.createMechanical(rs, targetPosition);
            rotorsLeftToRight.add(rotor);
        }

        // Reflector
        ReflectorSpec rf = spec.getReflectorById(config.reflectorId());
        Reflector reflector = reflectorFactory.create(rf);

        return new CodeImpl(
                alphabet,
                rotorsLeftToRight,        // left -> right
                reflector,
                rotorIdsLeftToRight,      // left -> right
                positionsLeftToRight,     // left -> right
                config.reflectorId()
        );
    }
}
