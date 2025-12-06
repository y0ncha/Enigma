package enigma.engine.factory;

import enigma.machine.component.rotor.RotorImpl;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;
import enigma.machine.component.code.Code;

/**
 * Factory contract for producing runtime {@link Code} objects from a
 * {@link MachineSpec} and configuration.
 *
 * <p>Implementations construct fully-initialized {@link Code} instances containing
 * rotors, reflector, and configuration metadata. The factory:</p>
 * <ul>
 *   <li>Selects rotor specifications by ID from the machine spec</li>
 *   <li>Creates runtime rotors using {@link RotorFactory}</li>
 *   <li>Creates the reflector using {@link ReflectorFactory}</li>
 *   <li>Assembles all components into a {@link Code} instance</li>
 * </ul>
 *
 * <h2>Ordering Convention</h2>
 * <p>Configuration objects (e.g., {@link CodeConfig}) are expressed in left→right
 * order (user-facing machine window view). The factory preserves this ordering
 * in the resulting {@link Code} object.</p>
 *
 * <h2>Validation</h2>
 * <p>Validation of inputs (spec/config) should be performed by the caller
 * (the engine); the factory focuses on object construction.</p>
 *
 * @since 1.0
 * @see RotorFactory
 * @see ReflectorFactory
 */
public interface CodeFactory {

    /**
     * Create a {@link Code} from an explicit configuration.
     *
     * <p>This is the primary factory method that creates codes using the
     * mechanical rotor model ({@link RotorImpl}).</p>
     *
     * @param spec machine specification containing rotor and reflector definitions
     * @param selection configuration specifying rotor IDs, positions, and reflector ID
     * @return fully initialized {@link Code} instance
     */
    Code create(MachineSpec spec, CodeConfig selection);
}
