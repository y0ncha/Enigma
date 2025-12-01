package enigma.engine.factory;

import enigma.engine.model.RotorSpec;
import enigma.machine.rotor.Rotor;

/**
 * Factory that creates runtime {@link Rotor} instances from {@link RotorSpec}.
 * Implementations are bound to a specific {@code Alphabet} at construction time.
 *
 * @since 1.0
 */
public interface RotorFactory {

    /**
     * Create a rotor instance for the provided spec using the factory's alphabet.
     *
     * @param spec rotor specification
     * @param startPosition initial rotor position (0-based)
     * @return created {@link Rotor}
     */
    Rotor create(RotorSpec spec, int startPosition);
}
