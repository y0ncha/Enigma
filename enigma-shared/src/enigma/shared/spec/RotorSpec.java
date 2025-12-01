package enigma.shared.spec;

import java.util.Objects;

/**
 * Represents the specification of a rotor in the Enigma machine.
 *
 * <p>Immutable data carrier implemented as a record. Arrays are defensively copied
 * to preserve immutability semantics for callers.</p>
 *
 * <p>Note: Explicit getter methods are provided for array fields to return defensive copies.
 * Use record accessors {@code id()} and {@code notchIndex()} for primitive fields.</p>
 *
 * @since 1.0
 */
public record RotorSpec(
        int id,
        int notchIndex,
        int[] forwardMapping,
        int[] backwardMapping
) {
    /**
     * Canonical constructor with validation and defensive copy.
     *
     * @param id rotor identifier
     * @param notchIndex notch position index (0-based)
     * @param forwardMapping forward mapping array (right→left)
     * @param backwardMapping backward mapping array (left→right)
     */
    public RotorSpec {
        Objects.requireNonNull(forwardMapping);
        Objects.requireNonNull(backwardMapping);
        // Defensive copy to preserve immutability semantics for callers
        forwardMapping = forwardMapping.clone();
        backwardMapping = backwardMapping.clone();
    }

    /**
     * Return a defensive copy of the forward mapping array.
     *
     * @return copy of forward mapping (right→left)
     */
    public int[] getForwardMapping() {
        return forwardMapping.clone();
    }

    /**
     * Return a defensive copy of the backward mapping array.
     *
     * @return copy of backward mapping (left→right)
     */
    public int[] getBackwardMapping() {
        return backwardMapping.clone();
    }
}
