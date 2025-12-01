package enigma.engine.components.factory;

import enigma.engine.components.dto.CodeConfig;
import enigma.engine.components.model.MachineSpec;
import enigma.machine.component.code.Code;

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
     * Create a new {@link Code} using random selection of components from the spec.
     * The method may sample rotors, reflector and starting positions.
     *
     * @param spec machine specification (must be valid)
     * @return created {@link Code}
     */
    Code createRandom(MachineSpec spec);

    /**
     * Create a {@link Code} from an explicit configuration.
     *
     * @param spec machine specification (must be valid)
     * @param selection configuration describing rotor ids, positions and reflector
     * @return created {@link Code}
     */
    Code create(MachineSpec spec, CodeConfig selection);
}
