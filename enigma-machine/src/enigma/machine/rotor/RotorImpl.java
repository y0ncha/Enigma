package enigma.machine.rotor;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical runtime rotor implementation using the mechanical column-rotation model.
 *
 * <p>This implementation represents the physical Enigma rotor wiring as two parallel
 * columns of alphabet indices:</p>
 * <ul>
 *   <li>{@code rightColumn} — the keyboard-facing contacts (entry side)</li>
 *   <li>{@code leftColumn} — the reflector-facing contacts (exit side on forward path)</li>
 * </ul>
 *
 * <p>At construction, {@code rightColumn} contains the identity sequence [0,1,2,...,N-1]
 * while {@code leftColumn} holds the wiring permutation from the specification. Rotation
 * is simulated by physically shifting the first row to the bottom of both columns,
 * exactly as a real Enigma rotor wheel would rotate.</p>
 *
 * <h2>Signal Processing</h2>
 * <ul>
 *   <li><b>Forward (FORWARD direction):</b> An entry index yields a symbol from
 *       {@code rightColumn}. We find that symbol's position in {@code leftColumn}
 *       and return that position as the exit index.</li>
 *   <li><b>Backward (BACKWARD direction):</b> An entry index yields a symbol from
 *       {@code leftColumn}. We find that symbol's position in {@code rightColumn}
 *       and return that position as the exit index.</li>
 * </ul>
 *
 * <h2>Stepping Behavior</h2>
 * <p>The {@link #advance()} method rotates both columns once and returns {@code true}
 * if the new top position equals the rotor's notch index, indicating that the next
 * rotor (to the left) should also advance.</p>
 *
 * <h2>Position</h2>
 * <p>The rotor's current position is defined as the value at the top of
 * {@code rightColumn} (i.e., {@code rightColumn.get(0)}). This represents
 * the letter visible in the machine window and determines the rotor's
 * electrical alignment with the rest of the circuit.</p>
 *
 * @since 1.0
 */
public class RotorImpl implements Rotor {

    private final List<Integer> rightColumn;  // keyboard-facing contacts
    private final List<Integer> leftColumn;   // reflector-facing contacts
    private final int alphabetSize;

    private final int notchIndex; // which row triggers stepping of next rotor
    private final int id;

    /**
     * Construct a rotor from forward mapping and notch index.
     *
     * <p>The forward mapping defines the wiring from right (keyboard side) to left
     * (reflector side). Specifically, {@code forwardMapping[i]} is the left-column
     * symbol that is wired to right-column symbol {@code i}.</p>
     *
     * <p>The mechanical model computes backward mappings dynamically by searching
     * the columns, so the backward mapping parameter is accepted for API consistency
     * with the specification but not stored.</p>
     *
     * @param forwardMapping mapping from right→left (base wiring); index = right symbol, value = left symbol
     * @param backwardMapping inverse mapping (left→right); accepted for API consistency, computed dynamically
     * @param notchIndex index at which the rotor triggers stepping of the next rotor (0..N-1)
     * @param alphabetSize size of alphabet (typically 26 for A-Z)
     * @param id rotor identifier for debugging/tracing
     */
    public RotorImpl(int[] forwardMapping, int[] backwardMapping, int notchIndex, int alphabetSize, int id) {
        this.alphabetSize = alphabetSize;
        // Build columns: rightColumn is identity [0,1,2,...], leftColumn holds the wiring
        this.rightColumn = new ArrayList<>(alphabetSize);
        this.leftColumn  = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            rightColumn.add(i);          // fixed identity column (A,B,C,...)
        }
        for (int i = 0; i < alphabetSize; i++) {
            leftColumn.add(forwardMapping[i]);
        }

        // Store notch (bounded)
        this.notchIndex = makeInBounds(notchIndex);
        this.id = id;
    }

    // ---------------------------------------------------------
    // Rotor interface implementation
    // ---------------------------------------------------------

    /**
     * Advance the rotor by one step (rotate both columns).
     *
     * <p>This method simulates the physical rotation of the rotor wheel by
     * shifting the first row of both columns to the bottom. After rotation,
     * the method checks whether the new top position equals the notch index.</p>
     *
     * @return {@code true} if the notch is engaged after stepping (i.e., the
     *         next rotor to the left should also advance); {@code false} otherwise
     */
    @Override
    public boolean advance() {
        rotate();

        // Return true if the NEW top letter equals notch position
        int pos = getPosition();
        return pos == notchIndex;
    }

    /**
     * Process a signal through the rotor in the given direction.
     *
     * <p>All indices are global alphabet indices (0..alphabetSize-1).</p>
     *
     * @param index input index representing the signal entry point
     * @param direction {@link Direction#FORWARD} for right→left (toward reflector),
     *                  {@link Direction#BACKWARD} for left→right (from reflector)
     * @return output index after passing through the rotor wiring
     */
    @Override
    public int process(int index, Direction direction) {
        return (direction == Direction.FORWARD)
                ? encodeForward(index)
                : encodeBackward(index);
    }

    /**
     * Get the current rotor position (the letter visible in the window).
     *
     * <p>The position is defined as the symbol at the top of {@code rightColumn},
     * which represents the rotor's rotational offset from its base configuration.</p>
     *
     * @return current position as an alphabet index (0..alphabetSize-1)
     */
    @Override
    public int getPosition() {
        // The top of rightColumn represents the current rotor window letter index
        return rightColumn.get(0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation rotates the rotor columns until the top of
     * {@code rightColumn} equals the target position.</p>
     */
    @Override
    public void setPosition(int pos) {
        pos = makeInBounds(pos);
        int safety = alphabetSize;
        while (getPosition() != pos && safety-- > 0) {
            rotate();
        }
        if (getPosition() != pos) {
            throw new IllegalStateException("Failed to reach position " + pos + " in RotorImpl");
        }
    }

    /**
     * Get the notch index that triggers stepping of the next rotor.
     *
     * <p>When {@link #advance()} causes the rotor to reach this position,
     * it returns {@code true} to signal that the next rotor should step.</p>
     *
     * @return notch position (0..alphabetSize-1)
     */
    @Override
    public int getNotchInd() {
        return notchIndex;
    }

    // ---------------------------------------------------------
    // Core encoding logic
    // ---------------------------------------------------------

    /**
     * Forward encoding: signal enters from the right (keyboard side).
     *
     * <p>The entry index selects a symbol from {@code rightColumn}. We find
     * where that symbol appears in {@code leftColumn} and return that index.</p>
     */
    private int encodeForward(int entryIndex) {
        // right entry gives the "symbol"
        int sym = rightColumn.get(entryIndex);
        // find where this symbol appears in left column
        int index = leftColumn.indexOf(sym);
        return index;
    }

    /**
     * Backward encoding: signal enters from the left (reflector side).
     *
     * <p>The entry index selects a symbol from {@code leftColumn}. We find
     * where that symbol appears in {@code rightColumn} and return that index.</p>
     */
    private int encodeBackward(int entryIndex) {
        int sym = leftColumn.get(entryIndex);
        int index = rightColumn.indexOf(sym);
        return index;
    }

    /**
     * Rotate both columns by shifting the first row to the bottom.
     * This simulates the physical rotation of a real Enigma rotor.
     */
    private void rotate() {
        int r0 = rightColumn.remove(0);
        int l0 = leftColumn.remove(0);

        rightColumn.add(r0);
        leftColumn.add(l0);
    }

    /**
     * Ensure the given value is within alphabet bounds [0, alphabetSize).
     */
    private int makeInBounds(int x) {
        x %= alphabetSize;
        return x < 0 ? x + alphabetSize : x;
    }
}
