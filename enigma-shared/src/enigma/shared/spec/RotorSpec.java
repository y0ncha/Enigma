package enigma.shared.spec;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private String mappingToString(int[] mapping) {
        return IntStream.range(0, mapping.length)
                .mapToObj(i -> (char)('A' + i) + "->" + (char)('A' + mapping[i]))
                .collect(Collectors.joining(", "));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return String.format("Rotor ID: %d, Notch: %d, Forward: [%s], Backward: [%s]",
                id, notchIndex + 1, mappingToString(forwardMapping), mappingToString(backwardMapping));
    }
}
