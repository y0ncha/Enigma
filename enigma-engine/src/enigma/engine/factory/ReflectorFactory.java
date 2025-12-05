package enigma.engine.factory;

import enigma.shared.spec.ReflectorSpec;
import enigma.machine.component.reflector.Reflector;

/**
 * Factory that creates runtime {@link Reflector} instances from {@link ReflectorSpec}.
 * Implementations are bound to a specific {@code Alphabet} at construction time.
 *
 * @since 1.0
 */
public interface ReflectorFactory {

    /**
     * Create a reflector instance for the provided spec using factory's alphabet.
     *
     * @param spec reflector specification
     * @return created {@link Reflector}
     */
    Reflector create(ReflectorSpec spec);
}
