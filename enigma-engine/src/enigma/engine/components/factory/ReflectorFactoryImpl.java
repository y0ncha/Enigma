package enigma.engine.components.factory;

import enigma.engine.components.model.ReflectorSpec;
import enigma.machine.component.alphabet.Alphabet;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.reflector.ReflectorImpl;

/**
 * Default {@link ReflectorFactory} implementation producing {@link ReflectorImpl}
 * instances bound to an {@link Alphabet}.
 *
 * The factory expects a non-null {@link Alphabet} at construction time and
 * produces reflectors using the spec's integer mapping.
 */
public class ReflectorFactoryImpl implements ReflectorFactory {

    private final Alphabet alphabet;

    public ReflectorFactoryImpl(Alphabet alphabet) {
        if (alphabet == null) throw new IllegalArgumentException("alphabet must not be null");
        this.alphabet = alphabet;
    }

    @Override
    public Reflector create(ReflectorSpec spec) {
        if (spec == null) throw new IllegalArgumentException("spec must not be null");
        int[] mapping = spec.getMapping();
        // ReflectorImpl expects the alphabet and a symmetric integer mapping
        return new ReflectorImpl(alphabet, mapping);
    }
}
