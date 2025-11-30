package enigma.engine.components.model;

/**
 * Represents the specification of a rotor in the Enigma machine.
 * Contains the rotor's unique identifier, notch position, and wiring mappings
 * for both forward and backward signal paths. This class is used to define
 * the static configuration of a rotor, which can then be instantiated and
 * used in the Enigma engine for encryption and decryption operations.
 */
public class RotorSpec {
    private final int id;
    private final int notchIndex;        // 0..N-1
    private final int[] forwardMapping;  // from right index -> left index
    private final int[] backwardMapping; // from left index -> right index

    public RotorSpec(int id,
                     int notchIndex,
                     int[] forwardMapping,
                     int[] backwardMapping) {
        this.id = id;
        this.notchIndex = notchIndex;
        this.forwardMapping = forwardMapping;
        this.backwardMapping = backwardMapping;
    }

    public int getId() {
        return id;
    }

    public int getNotchIndex() {
        return notchIndex;
    }

    public int[] getForwardMapping() {
        return forwardMapping.clone();
    }

    public int[] getBackwardMapping() {
        return backwardMapping.clone();
    }
}
