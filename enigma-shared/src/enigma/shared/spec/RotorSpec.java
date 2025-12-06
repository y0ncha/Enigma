package enigma.shared.spec;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the specification of a rotor in the Enigma machine.
 *
 * <p><b>Module:</b> enigma-shared (specs)</p>
 *
 * <p>Immutable data carrier implemented as a record. Arrays are defensively copied
 * to preserve immutability semantics for callers.</p>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li><b>id:</b> Rotor identifier (1..N from XML)</li>
 *   <li><b>notchIndex:</b> 0-based index where notch engages (triggers next rotor)</li>
 *   <li><b>rightColumn:</b> Char array for right (keyboard-facing) contacts</li>
 *   <li><b>leftColumn:</b> Char array for left (reflector-facing) contacts</li>
 * </ul>
 *
 * <h2>Column Semantics</h2>
 * <p>The right/left column arrays represent rotor rows in <b>XML order</b>
 * (top→bottom as parsed from &lt;BTE-Positioning&gt;). Each entry is a
 * character from the alphabet. Columns are NOT reordered by loader or factory.</p>
 *
 * <h2>Notch Index</h2>
 * <p>The notchIndex is 0-based (converted from 1-based XML notch value).
 * When the rotor's window position equals the notch, the next rotor (to the left)
 * also advances.</p>
 *
 * <p><b>Note:</b> Explicit getter methods are provided for array fields to return
 * defensive copies. Use record accessors {@code id()} and {@code notchIndex()}
 * for primitive fields.</p>
 *
 * @param id rotor identifier (1..N)
 * @param notchIndex 0-based notch position (0..alphabetSize-1)
 * @param rightColumn char array for right column (keyboard side)
 * @param leftColumn char array for left column (reflector side)
 * @since 1.0
 */
public record RotorSpec(
        int id,
        int notchIndex,
        char[] rightColumn,
        char[] leftColumn
) {
    /**
     * Canonical constructor with validation and defensive copy.
     *
     * <p>The right/left column arrays represent the rotor rows in the same order
     * as they appear in the XML &lt;BTE-Positioning&gt; sequence (top→bottom).
     * Each entry is a character from the alphabet.</p>
     *
     * @param id rotor identifier (1..N)
     * @param notchIndex 0-based notch position
     * @param rightColumn right column chars (keyboard side)
     * @param leftColumn left column chars (reflector side)
     */
    public RotorSpec {
        Objects.requireNonNull(rightColumn);
        Objects.requireNonNull(leftColumn);
        rightColumn = rightColumn.clone();
        leftColumn = leftColumn.clone();
    }

    /**
     * Return a defensive copy of the right-column char array (row order).
     *
     * @return cloned right column array
     */
    public char[] getRightColumn() { return rightColumn.clone(); }

    /**
     * Return a defensive copy of the left-column char array (row order).
     *
     * @return cloned left column array
     */
    public char[] getLeftColumn() { return leftColumn.clone(); }

    /**
     * Build a string representation of rotor rows (right→left pairs).
     *
     * @return comma-separated list of right→left pairs
     */
    private String rowsToString() {
        int n = Math.min(rightColumn.length, leftColumn.length);
        return IntStream.range(0, n)
                .mapToObj(i -> rightColumn[i] + "->" + leftColumn[i])
                .collect(Collectors.joining(", "));
    }

    /**
     * Generate a string representation of the rotor spec.
     *
     * <p>Format: "Rotor ID: {id}, Notch: {1-based}, Rows: [right→left pairs]"</p>
     *
     * @return formatted rotor spec string
     */
    @Override
    public String toString() {
        return String.format("Rotor ID: %d, Notch: %d, Rows: [%s]", id, notchIndex + 1, rowsToString());
    }
}
