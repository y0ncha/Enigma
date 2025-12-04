package enigma.engine.factory;

import enigma.shared.dto.config.CodeConfig;
import enigma.shared.spec.MachineSpec;
import enigma.machine.code.Code;

/**
 * Factory contract for producing runtime {@link Code} objects from a
 * {@link MachineSpec} and configuration.
 *
 * <p>Implementations are expected to construct fully-initialized {@link Code}
 * instances. Validation of inputs (spec/config) should be performed by the
 * caller (the engine); the factory focuses on object construction.</p>
 *
 * Ordering note: configuration objects (e.g. {@link CodeConfig}) are expressed
 * in left→right order (user-facing); factories may reorder values for runtime
 * (right→left) as needed.
 *
 * @since 1.0
 */
public interface CodeFactory {

    /**
     * Create a {@link Code} from an explicit configuration.
     *
     * @param spec machine specification (must be valid)
     * @param selection configuration describing rotor ids, positions and reflector
     * @return created {@link Code}
     */
    Code createVirtual(MachineSpec spec, CodeConfig selection);

    Code createMechanical(MachineSpec spec, CodeConfig selection);
}
