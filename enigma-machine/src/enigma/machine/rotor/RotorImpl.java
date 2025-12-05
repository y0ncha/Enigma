package enigma.machine.rotor;

import java.util.LinkedList;

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

    // Wire pairs represent one row in the physical rotor: right (keyboard) and left (reflector)
    private record Wire(int right, int left) {}

    private final LinkedList<Wire> wires;  // rows in top→bottom order
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
     * the wires list, so the backward mapping parameter is accepted for API consistency
     * with the specification but not stored. We perform a light sanity-check on the
     * backward mapping to catch malformed specs early.</p>
     *
     * @param forwardMapping mapping from right→left (base wiring); index = right symbol, value = left symbol
     * @param backwardMapping inverse mapping (left→right); accepted for API consistency, validated for length
     * @param notchIndex index at which the rotor triggers stepping of the next rotor (0..N-1)
     * @param alphabetSize size of alphabet (typically 26 for A-Z)
     * @param id rotor identifier for debugging/tracing
     */
    public RotorImpl(int[] forwardMapping, int[] backwardMapping, int notchIndex, int alphabetSize, int id) {
        this.alphabetSize = alphabetSize;
        // Basic validation of backwardMapping to make use of the parameter and catch spec errors
        if (backwardMapping == null) throw new IllegalArgumentException("backwardMapping must not be null");
        if (backwardMapping.length != alphabetSize)
            throw new IllegalArgumentException("backwardMapping length must equal alphabetSize");

        // Build wires list: right is identity [0,1,2,...], left comes from forwardMapping
        this.wires = new LinkedList<>();

        for (int i = 0; i < alphabetSize; i++) {
            wires.add(new Wire(i, forwardMapping[i]));
        }

        // Store notch (bounded)
        this.notchIndex = makeInBounds(notchIndex);
        this.id = id;
    }

    // ---------------------------------------------------------
    // Rotor interface implementation
    // ---------------------------------------------------------

    /**
     * Advance the rotor by one step (rotate wires list).
     *
     * <p>This method simulates the physical rotation of the rotor wheel by
     * moving the first row to the bottom. After rotation, the method checks
     * whether the new top position equals the notch index.</p>
     *
     * @return {@code true} if the notch is engaged after stepping (i.e., the
     *         next rotor to the left should also advance); {@code false} otherwise
     */
    @Override
    public boolean advance() {
        rotate();
        int pos = getPosition();
        return pos == notchIndex;
    }

    /**
     * Process a signal through the rotor in the given direction.
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
     * <p>The position is defined as the right value of the top wire.</p>
     */
    @Override
    public int getPosition() {
        return wires.getFirst().right();
    }

    /**
     * Set the rotor to a specific position by rotating until the top right matches.
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
     */
    @Override
    public int getNotchInd() {
        return notchIndex;
    }

    /**
     * Get the rotor's identifier.
     */
    @Override
    public int getId() {
        return id;
    }

    // ---------------------------------------------------------
    // Core encoding logic using wires list
    // ---------------------------------------------------------

    private int encodeForward(int entryIndex) {
        int sym = wires.get(entryIndex).right();
        // find where this symbol appears as left in wires
        for (int i = 0; i < wires.size(); i++) {
            if (wires.get(i).left() == sym) return i;
        }
        throw new IllegalStateException("Forward encoding: symbol not found in left column: " + sym);
    }

    private int encodeBackward(int entryIndex) {
        int sym = wires.get(entryIndex).left();
        for (int i = 0; i < wires.size(); i++) {
            if (wires.get(i).right() == sym) return i;
        }
        throw new IllegalStateException("Backward encoding: symbol not found in right column: " + sym);
    }

    private void rotate() {
        Wire top = wires.removeFirst();
        wires.addLast(top);
    }

    private int makeInBounds(int x) {
        x %= alphabetSize;
        return x < 0 ? x + alphabetSize : x;
    }
}
