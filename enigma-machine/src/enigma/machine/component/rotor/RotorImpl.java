package enigma.machine.component.rotor;

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

    private final LinkedList<Wire> wires;  // rows in top → bottom order (chars)
    private final int alphabetSize;
    private final char notch;
    private final int id;

    /**
     * Construct a rotor from row-ordered right/left columns and a notch index.
     *
     * <p>The {@code rightColumn} and {@code leftColumn} arrays contain the symbol
     * indices for each row in top→bottom order as parsed from the XML
     * <BTE-Positioning> entries. Both arrays must have the same length equal to the
     * alphabet size.</p>
     *
     * @param rightColumn row-ordered right-column symbols (0-based indices)
     * @param leftColumn row-ordered left-column symbols (0-based indices)
     * @param notchIndex index at which the rotor triggers stepping of the next rotor (0..N-1)
     * @param alphabetSize machine alphabet size used for bounds
     * @param id rotor identifier for debugging/tracing
     */
    public RotorImpl(char[] rightColumn, char[] leftColumn, int notchIndex, int alphabetSize, int id) {

        this.alphabetSize = alphabetSize;

        if (rightColumn == null || leftColumn == null) throw new IllegalArgumentException("right/left column must not be null");
        if (rightColumn.length != this.alphabetSize || leftColumn.length != this.alphabetSize)
            throw new IllegalArgumentException("Rotor column lengths must equal alphabet size");

        this.wires = new LinkedList<>();
        // Build wires list using provided row-ordered char columns (top->bottom)
        for (int i = 0; i < this.alphabetSize; i++) {
            wires.add(new Wire(rightColumn[i], leftColumn[i]));
        }

        // Store notch (bounded)
        this.notch = wires.get(notchIndex).right();
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
        char pos = getPosition();
        return pos == notch;
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
    public char getPosition() {
        // return the right-side character at the top row
        return wires.getFirst().right();
    }

    /**
     * Set the rotor to a specific position by rotating until the top right matches.
     */
    @Override
    public void setPosition(char pos) {
        int safety = alphabetSize;
        while (getPosition() != pos && safety-- > 0) {
            rotate();
        }
        if (getPosition() != pos) {
            throw new IllegalStateException("Failed to reach position " + pos + " in rotor: " + id);
        }
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
        if (entryIndex < 0 || entryIndex >= alphabetSize) {
            throw new IllegalArgumentException("Invalid entry index: " + entryIndex);
        }

        // Forward path: RIGHT value at row → find its location in LEFT column
        char inChar = wires.get(entryIndex).right();

        for (int i = 0; i < wires.size(); i++) {
            if (wires.get(i).left() == inChar) {
                return i;
            }
        }
        throw new IllegalStateException("Forward encoding: symbol not found in left column: " + inChar);
    }

    private int encodeBackward(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= alphabetSize) {
            throw new IllegalArgumentException("Invalid entry index: " + entryIndex);
        }

        // Backward path: LEFT value at row → find its location in RIGHT column
        char inChar = wires.get(entryIndex).left();

        for (int i = 0; i < wires.size(); i++) {
            if (wires.get(i).right() == inChar) {
                return i;
            }
        }
        throw new IllegalStateException("Backward encoding: symbol not found in right column: " + inChar);
    }

    private void rotate() {
        Wire top = wires.removeFirst();
        wires.addLast(top);
    }

    @Override
    public Wire getWire(int row) {
            return wires.get(row);
    }

    @Override
    public String toString() {
        // Render a single rotor column: small ID box above, then a tall box with L | R per row.
        final int colInner = 9;
        StringBuilder sb = new StringBuilder();

        java.util.function.BiFunction<String,Integer,String> center = (s,w) -> {
            if (s == null) s = "";
            if (s.length() >= w) return s.substring(0,w);
            int left = (w - s.length())/2;
            int right = w - s.length() - left;
            return " ".repeat(left) + s + " ".repeat(right);
        };

        // total line width for this column (2 leading + border + inner + border) = colInner + 4
        final int colWidth = colInner + 4;
        java.util.function.BiFunction<String,Integer,String> rpad = (s,w) -> {
            if (s == null) s = "";
            if (s.length() >= w) return s;
            return s + " ".repeat(w - s.length());
        };

        // small ID box (pad each line to fixed width)
        sb.append(rpad.apply("  ┌" + "─".repeat(colInner) + "┐", colWidth)).append('\n');
        sb.append(rpad.apply("  │" + center.apply("Rotor " + id, colInner) + "│", colWidth)).append('\n');
        sb.append(rpad.apply("  └" + "─".repeat(colInner) + "┘", colWidth)).append('\n');

        // main tall box top
        sb.append(rpad.apply("  ┌" + "─".repeat(colInner) + "┐", colWidth)).append('\n');

        // data rows: render using the stored chars in wires (left | right)
        for (Wire w : wires) {
            char leftChar = w.left();
            char rightChar = w.right();
            String row = String.format("%c | %c", leftChar, rightChar);
            String line = "  │" + center.apply(row, colInner) + "│";
            sb.append(rpad.apply(line, colWidth)).append('\n');
        }

        // bottom
        sb.append(rpad.apply("  └" + "─".repeat(colInner) + "┘", colWidth)).append('\n');

        return sb.toString();
    }
}
