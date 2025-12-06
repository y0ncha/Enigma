package enigma.machine.component.reflector;

import java.lang.String;

/**
 * Simple runtime reflector using a symmetric mapping array.
 *
 * @since 1.0
 */
public class ReflectorImpl implements Reflector {

    private final int[] mapping;   // symmetric mapping array
    private final String id;

    /**
     * Create reflector with the provided alphabet and mapping.
     *
     * @param mapping symmetric mapping array (mapping[i] = j and mapping[j] = i)
     * @since 1.0
     */
    public ReflectorImpl(int[] mapping, String id) {
        this.mapping = mapping;
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(int index) {
        return mapping[index];
    }

    @Override
    public String getId() {
        return id;
    }


}