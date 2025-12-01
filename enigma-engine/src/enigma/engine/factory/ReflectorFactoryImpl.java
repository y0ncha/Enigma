package enigma.engine.factory;

import enigma.engine.model.ReflectorSpec;
import enigma.machine.alphabet.Alphabet;
import enigma.machine.reflector.Reflector;
import enigma.machine.reflector.ReflectorImpl;

/**
 * Default {@link ReflectorFactory} implementation producing {@link ReflectorImpl}
 * instances bound to an {@link Alphabet}.
 *
 * <p>The factory expects a non-null {@link Alphabet} at construction time and
 * produces reflectors using the spec's integer mapping.</p>
 *
 * @since 1.0
 */
public class ReflectorFactoryImpl implements ReflectorFactory {

    private final Alphabet alphabet;

    /**
     * Create a reflector factory bound to the given alphabet.
     *
     * @param alphabet alphabet used for reflector construction
     * @throws IllegalArgumentException if alphabet is null
     */
    public ReflectorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reflector create(ReflectorSpec spec) {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");
        int[] mapping = spec.mapping();
        // ReflectorImpl expects the alphabet and a symmetric integer mapping
        return new ReflectorImpl(alphabet, mapping);
    }
}
