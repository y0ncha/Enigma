package enigma.machine.component.reflector;

/**
 * Reflector maps an internal index to its paired index.
 *
 * Implementations must provide a symmetric pairwise mapping.
 *
 * @since 1.0
 */
public interface Reflector {
    /**
     * Transform the input index through the reflector mapping.
     *
     * @param input index to reflect
     * @return reflected index
     * @since 1.0
     */
    int process(int input);
}
