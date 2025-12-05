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
 * <p>This factory constructs {@link Code} instances using the mechanical rotor
 * model ({@link enigma.machine.rotor.RotorImpl}). The factory:</p>
 * <ul>
 *   <li>Creates a {@link RotorFactory} for the given alphabet</li>
 *   <li>Builds rotors in left→right order from the configuration</li>
 *   <li>Creates the reflector from the specification</li>
 *   <li>Assembles all components into a {@link CodeImpl}</li>
 * </ul>
 *
 * <h2>Ordering Conventions</h2>
 * <ul>
 *   <li>All {@link CodeConfig} inputs (rotor IDs and initial positions)
 *       are given in <b>left→right</b> order, exactly as they appear in the
 *       machine window and in the XML definition.</li>
 *   <li>The factory <b>preserves left→right ordering</b>. It does not reverse
 *       rotor IDs or positions.</li>
 *   <li>The runtime {@link Code} object stores rotors in left→right order.
 *       The {@code Machine} is responsible for iterating rotors in the correct
 *       physical direction during processing:
 *       <ul>
 *         <li>Forward signal: right→left (iterate high index → 0)</li>
 *         <li>Backward signal: left→right (iterate 0 → high index)</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p>This design aligns all internal structures with the XML/user-facing
 * representation and avoids unnecessary list reversals.</p>
 *
 * <h2>Validation</h2>
 * <p>The factory assumes that all validation is performed by the caller
 * (typically {@link EngineImpl}).</p>
 *
 * @since 1.0
 * @see RotorFactory
 * @see ReflectorFactory
 */
public class CodeFactoryImpl implements CodeFactory {

    /**
     * {@inheritDoc}
     *
     * <p>This is the primary factory method that creates codes using the
     * mechanical rotor model.</p>
     */
    @Override
    public Code create(MachineSpec spec, CodeConfig config) {
        Alphabet alphabet = spec.alphabet();
        RotorFactory rotorFactory = new RotorFactoryImpl(alphabet);
        ReflectorFactory reflectorFactory = new ReflectorFactoryImpl(alphabet);

        // Build rotors in left→right order
        List<Rotor> rotors = buildRotors(spec, config, rotorFactory);

        // Build reflector
        Reflector reflector = buildReflector(spec, config, reflectorFactory);

        // Assemble code (preserving left→right order)
        return new CodeImpl(
                alphabet,
                rotors,
                reflector,
                new ArrayList<>(config.rotorIds()),
                new ArrayList<>(config.initialPositions()),
                config.reflectorId()
        );
    }

    // ---------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------

    /**
     * Build rotors from the specification and configuration.
     *
     * @param spec machine specification
     * @param config code configuration
     * @param rotorFactory factory for creating rotors
     * @return list of rotors in left→right order
     */
    private List<Rotor> buildRotors(MachineSpec spec, CodeConfig config, RotorFactory rotorFactory) {
        List<Integer> rotorIds = config.rotorIds();
        List<Integer> positions = config.initialPositions();
        List<Rotor> rotors = new ArrayList<>(rotorIds.size());

        for (int i = 0; i < rotorIds.size(); i++) {
            int rotorId = rotorIds.get(i);
            RotorSpec rotorSpec = spec.getRotorById(rotorId);
            int targetPosition = positions.get(i);

            Rotor rotor = rotorFactory.create(rotorSpec, targetPosition);
            rotors.add(rotor);
        }

        return rotors;
    }

    /**
     * Build the reflector from the specification and configuration.
     *
     * @param spec machine specification
     * @param config code configuration
     * @param reflectorFactory factory for creating reflectors
     * @return reflector instance
     */
    private Reflector buildReflector(MachineSpec spec, CodeConfig config, ReflectorFactory reflectorFactory) {
        ReflectorSpec reflectorSpec = spec.getReflectorById(config.reflectorId());
        return reflectorFactory.create(reflectorSpec);
    }
}
