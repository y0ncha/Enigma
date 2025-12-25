package enigma.machine.component.plugboard;

/**
 * Array-based implementation of symmetric plugboard mapping.
 * <p>
 * Initially maps each index to itself (identity). Plugs create bidirectional swaps.
 * <p>
 * Invariants:
 * <ul>
 * <li>Mapping is always symmetric: if {@code mapping[a] = b} then {@code mapping[b] = a}</li>
 * <li>Each character can be plugged at most once</li>
 * <li>Self-mapping is forbidden</li>
 * </ul>
 */
public class PlugboardImpl implements Plugboard {

    private final int[] mapping;

    /**
     * Creates a plugboard initialized to identity mapping.
     *
     * @param alphabetSize size of the alphabet (number of characters)
     */
    public PlugboardImpl(int alphabetSize) {
        mapping = new int[alphabetSize];
        for (int i = 0; i < alphabetSize; i++) {
            mapping[i] = i; // Initialize as identity
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int swap(int idx) {
        return mapping[idx];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void plug(int a, int b) {

        // Self plugging is not allowed
        if (a == b) {
            throw new IllegalArgumentException("Cannot plug '" + a + "' to itself");
        }
        // Check if either character is already plugged
        if (mapping[a] != a) {
            throw new IllegalArgumentException("'" + a + " is already plugged to '" + mapping[a] + "'");
        }
        if (mapping[b] != b) {
            throw new IllegalArgumentException("'" + b + "' is already plugged to '" + mapping[b] + "'");
        }
        // Perform the symmetric pluging
        mapping[a] = b;
        mapping[b] = a;
    }
}
