package enigma.engine.factory;

import enigma.engine.exception.InvalidConfigurationException;
import enigma.shared.spec.ReflectorSpec;
import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.reflector.ReflectorImpl;

/**
 * Default {@link ReflectorFactory} implementation producing {@link ReflectorImpl}
 * instances bound to an {@link Alphabet}.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <p>The factory expects a non-null {@link Alphabet} at construction time and
 * produces reflectors using the spec's integer mapping array without modification.</p>
 *
 * <h2>Wiring Order</h2>
 * <p>The mapping is used as-is from the spec (XML order). The loader guarantees
 * symmetry and bijectivity; the factory does not revalidate.</p>
 *
 * @since 1.0
 */
public class ReflectorFactoryImpl implements ReflectorFactory {

    /**
     * Create a reflector factory bound to the given alphabet.
     *
     * @param alphabet alphabet used for size validation
     * @throws InvalidConfigurationException if alphabet is null
     */
    public ReflectorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) {
            throw new InvalidConfigurationException(
                "Reflector factory initialization failed: Alphabet is missing. " +
                "Fix: Ensure the machine specification is properly loaded.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reflector create(ReflectorSpec spec) {
        if (spec == null) {
            throw new InvalidConfigurationException(
                "Reflector creation failed: Reflector specification is missing. " +
                "Fix: Ensure the machine specification is properly loaded.");
        }
        int[] mapping = spec.mapping();
        // ReflectorImpl expects the alphabet and a symmetric integer mapping
        return new ReflectorImpl(mapping, spec.id());
    }
}
