package enigma.engine.factory;

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
     * @throws IllegalArgumentException if alphabet is null
     */
    public ReflectorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
    }

    /**
     * Create a reflector from the given specification.
     *
     * <p>The mapping array is used directly from the spec without modification
     * or reordering. Symmetry and bijectivity are assumed to be validated by
     * the loader.</p>
     *
     * @param spec reflector specification with ID and symmetric mapping
     * @return {@link ReflectorImpl} instance
     * @throws IllegalArgumentException if spec is null
     */
    @Override
    public Reflector create(ReflectorSpec spec) {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");
        int[] mapping = spec.mapping();
        // ReflectorImpl expects the alphabet and a symmetric integer mapping
        return new ReflectorImpl(mapping, spec.id());
    }
}
