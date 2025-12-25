package enigma.engine.factory;

import enigma.shared.spec.ReflectorSpec;
import enigma.machine.component.reflector.Reflector;

/**
 * Factory that creates runtime {@link Reflector} instances from {@link ReflectorSpec}.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>Implementations are bound to a specific {@code Alphabet} at construction time
 * and produce reflectors with symmetric mappings as defined by the spec.</p>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>Factory assumes spec is valid (loader validates symmetry/bijectivity)</li>
 *   <li>Mapping size must equal alphabet size</li>
 * </ul>
 *
 * @since 1.0
 */
public interface ReflectorFactory {

    /**
     * Create a reflector instance for the provided spec.
     *
     * @param spec reflector specification with ID and symmetric mapping
     * @return created {@link Reflector}
     */
    Reflector create(ReflectorSpec spec);
}
