package enigma.machine.reflector;

import enigma.machine.alphabet.Alphabet;

/**
 * Simple runtime reflector using a symmetric mapping array.
 *
 * @since 1.0
 */
public class ReflectorImpl implements Reflector {

    private final int[] mapping;   // symmetric mapping array

    /**
     * Create reflector with the provided alphabet and mapping.
     *
     * @param mapping symmetric mapping array (mapping[i] = j and mapping[j] = i)
     * @since 1.0
     */
    public ReflectorImpl(int[] mapping) {
        this.mapping = mapping;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(int index) {
        return mapping[index];
    }
}