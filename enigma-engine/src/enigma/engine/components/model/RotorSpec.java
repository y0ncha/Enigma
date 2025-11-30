package enigma.engine.components.model;

import java.util.Objects;

/**
 * Represents the specification of a rotor in the Enigma machine.
 * Immutable data carrier implemented as a record. Arrays are defensively copied.
 */
public record RotorSpec(
        int id,
        int notchIndex,
        int[] forwardMapping,
        int[] backwardMapping
) {
    public RotorSpec {
        Objects.requireNonNull(forwardMapping);
        Objects.requireNonNull(backwardMapping);
        // Defensive copy to preserve immutability semantics for callers
        forwardMapping = forwardMapping.clone();
        backwardMapping = backwardMapping.clone();
    }

    // Keep compatibility with existing code that expects getX() methods
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
