package enigma.engine.factory;

import enigma.shared.spec.RotorSpec;
import enigma.machine.rotor.Rotor;

/**
 * Factory for creating runtime {@link Rotor} instances from {@link RotorSpec}.
 *
 * <p>The factory is responsible only for adapting the immutable specification
 * into a concrete runtime rotor, preserving the directional semantics of the
 * mappings:</p>
 * <ul>
 *   <li>{@code forwardMapping}: right→left</li>
 *   <li>{@code backwardMapping}: left→right</li>
 * </ul>
 */
public interface RotorFactory {

    /**
     * Create a {@link Rotor} from the given specification and starting position.
     *
     * @param spec          rotor specification (id, notch index, mappings)
     * @param startPosition initial window position (0-based index into alphabet)
     * @return a runtime rotor instance
     */
    Rotor createVirtual(RotorSpec spec, int startPosition);

    Rotor createMechanical(RotorSpec spec, int startPosition);
}
