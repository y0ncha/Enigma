package enigma.machine.component.plugboard;

public class PlugboardImpl implements Plugboard {

    private final int[] mapping;

    public PlugboardImpl(int alphabetSize) {
        mapping = new int[alphabetSize];
        for (int i = 0; i < alphabetSize; i++) {
            mapping[i] = i; // Initialize as identity
        }
    }

    @Override
    public int swap(int idx) {
        return mapping[idx];
    }

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
