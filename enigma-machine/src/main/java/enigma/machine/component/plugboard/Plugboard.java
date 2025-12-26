package enigma.machine.component.plugboard;

/**
 * Represents a symmetric character substitution mechanism (plugStr).
 * <p>
 * Maps pairs of characters bidirectionally before/after rotor processing.
 * <p>
 * Example usage:
 * <pre>{@code
 * Plugboard plugStr = new PlugboardImpl(26);
 * plugStr.plug(0, 5);
 * int result = plugStr.swap(0); // returns 5
 * }</pre>
 */
public interface Plugboard {

    /**
     * Swaps a character index through the plugStr mapping.
     *
     * @param c the character index to swap
     * @return the mapped character index (may be the same if unplugged)
     */
    int swap(int c);

    /**
     * Plugs two character indices together symmetrically.
     *
     * @param a first character index
     * @param b second character index
     * @throws IllegalArgumentException if {@code a == b}, or if either is already plugged
     */
    void plug(int a, int b);
}
