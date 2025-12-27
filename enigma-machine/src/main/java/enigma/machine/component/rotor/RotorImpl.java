package enigma.machine.component.rotor;

import java.util.LinkedList;

/**
 * Rotor implementation using column-rotation model.
 *
 * <p>Maintains wiring as parallel left and right columns that rotate
 * physically to simulate mechanical rotor behavior. Supports forward
 * and backward signal processing with stepping and notch detection.</p>
 *
 * @since 1.0
 */
public class RotorImpl implements Rotor {

    private final LinkedList<Wire> wires;  // rows in top → bottom order (chars)
    private final int alphabetSize;
    private final char notch;
    private final int id;

    /**
     * Construct rotor from column arrays and notch position.
     *
     * @param rightColumn right-side wiring in row order
     * @param leftColumn left-side wiring in row order
     * @param notchIndex notch position triggering next rotor step
     * @param alphabetSize alphabet size for validation
     * @param id rotor identifier
     */
    public RotorImpl(char[] rightColumn, char[] leftColumn, int notchIndex, int alphabetSize, int id) {

        this.alphabetSize = alphabetSize;

        if (rightColumn == null || leftColumn == null) throw new IllegalArgumentException("Rotor columns cannot be null");
        if (rightColumn.length != this.alphabetSize || leftColumn.length != this.alphabetSize)
            throw new IllegalArgumentException("Rotor columns must match alphabet size");

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
     * {@inheritDoc}
     */
    @Override
    public boolean advance() {
        rotate();
        char pos = getPosition();
        return pos == notch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(int index, Direction direction) {
        return (direction == Direction.FORWARD)
                ? encodeForward(index)
                : encodeBackward(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char getPosition() {
        return wires.getFirst().right();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(char pos) {
        int safety = alphabetSize;
        while (getPosition() != pos && safety-- > 0) {
            rotate();
        }
        if (getPosition() != pos) {
            throw new IllegalStateException("Unable to reach position " + pos + " in rotor " + id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getId() {
        return id;
    }

    // ---------------------------------------------------------
    // Core encoding logic using wires list
    // ---------------------------------------------------------

    /**
     * Transform signal forward through rotor (keyboard to reflector).
     *
     * @param entryIndex entry row index
     * @return exit row index
     */
    private int encodeForward(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= alphabetSize) {
            throw new IllegalArgumentException("Entry index out of range: " + entryIndex);
        }

        char inChar = wires.get(entryIndex).right();

        for (int i = 0; i < wires.size(); i++) {
            if (wires.get(i).left() == inChar) {
                return i;
            }
        }
        throw new IllegalStateException("Symbol not found in left column: " + inChar);
    }

    /**
     * Transform signal backward through rotor (reflector to keyboard).
     *
     * @param entryIndex entry row index
     * @return exit row index
     */
    private int encodeBackward(int entryIndex) {
        if (entryIndex < 0 || entryIndex >= alphabetSize) {
            throw new IllegalArgumentException("Entry index out of range: " + entryIndex);
        }

        char inChar = wires.get(entryIndex).left();

        for (int i = 0; i < wires.size(); i++) {
            if (wires.get(i).right() == inChar) {
                return i;
            }
        }
        throw new IllegalStateException("Symbol not found in right column: " + inChar);
    }

    /**
     * Rotate rotor by moving top row to bottom.
     */
    private void rotate() {
        Wire top = wires.removeFirst();
        wires.addLast(top);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wire getWire(int row) {
            return wires.get(row);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int notchDist() {
        int index = 0;
        for (Wire w : wires) {
            if (w.right() == notch) {
                return index;
            }
            index++;
        }
        throw new IllegalStateException("Notch not found in wires list");
    }

    /**
     * Generate a visual column representation of the rotor wiring.
     *
     * <p>Displays left | right character pairs for each row in the rotor's
     * current rotational state. Rows are shown top-to-bottom matching the
     * internal wires list order.</p>
     *
     * @return multi-line string column for wiring display
     */
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
