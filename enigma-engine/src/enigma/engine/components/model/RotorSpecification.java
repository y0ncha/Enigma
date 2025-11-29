package enigma.engine.components.model;

public class RotorSpecification {
    private final int id;
    private final int notchIndex;        // 0..N-1
    private final int[] forwardMapping;  // from right index -> left index
    private final int[] backwardMapping; // from left index -> right index

    public RotorSpecification(int id,
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
        return forwardMapping;
    }

    public int[] getBackwardMapping() {
        return backwardMapping;
    }
}
