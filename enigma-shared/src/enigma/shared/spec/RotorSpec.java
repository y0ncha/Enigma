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
        int[] rightColumn,
        int[] leftColumn
) {
    /**
     * Canonical constructor with validation and defensive copy.
     * The right/left column arrays represent the rotor rows in the same order
     * as they appear in the XML <BTE-Positioning> sequence (top→bottom). Each
     * entry is a numeric alphabet index (0-based).
     */
    public RotorSpec {
        Objects.requireNonNull(rightColumn);
        Objects.requireNonNull(leftColumn);
        // Defensive copies
        rightColumn = rightColumn.clone();
        leftColumn = leftColumn.clone();
    }

    /**
     * Return a defensive copy of the right-column array (row order).
     */
    public int[] getRightColumn() { return rightColumn.clone(); }

    /**
     * Return a defensive copy of the left-column array (row order).
     */
    public int[] getLeftColumn() { return leftColumn.clone(); }

    private String rowsToString() {
        // Represent rows as R->L using A+index (best-effort display)
        return IntStream.range(0, Math.min(rightColumn.length, leftColumn.length))
                .mapToObj(i -> (char)('A' + rightColumn[i]) + "->" + (char)('A' + leftColumn[i]))
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return String.format("Rotor ID: %d, Notch: %d, Rows: [%s]", id, notchIndex + 1, rowsToString());
    }
}
