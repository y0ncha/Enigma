package enigma.machine.rotor;

import java.util.ArrayList;
import java.util.List;

/**
 * Rotor implementation using rotating right/left columns instead of index shifting.
 * Wiring is represented by two parallel columns:
 *   rightColumn[i] ↔ leftColumn[i]
 * Rotation physically shifts both columns, matching real Enigma behavior.
 *
 * @since 1.0
 */
public class MechanicalRotor implements Rotor {

    private final List<Integer> rightColumn;  // keyboard-facing contacts
    private final List<Integer> leftColumn;   // inward-facing contacts
    private final int alphabetSize;

    private int notchIndex; // which row triggers stepping of next rotor

    /**
     * Construct a rotor from forward mapping and notch index.
     *
     * @param forwardMapping mapping from right→left (base wiring)
     * @param notchIndex index at which the rotor triggers stepping (0..N-1)
     * @param alphabetSize size of alphabet
     */
    public MechanicalRotor(int[] forwardMapping, int notchIndex, int alphabetSize) {
        this.alphabetSize = alphabetSize;

        // Build inverse mapping (left → right)
        int[] inverse = new int[alphabetSize];
        for (int r = 0; r < alphabetSize; r++) {
            int leftVal = forwardMapping[r];
            inverse[leftVal] = r;
        }

        // Build columns
        this.rightColumn = new ArrayList<>(alphabetSize);
        this.leftColumn  = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            rightColumn.add(i);          // fixed identity column (A,B,C,...)
            leftColumn.add(inverse[i]);  // rotated via inverse mapping
        }

        // Store notch
        this.notchIndex = makeInBounds(notchIndex);
    }

    // ---------------------------------------------------------
    // Interface compliance
    // ---------------------------------------------------------

    @Override
    public boolean advance() {
        rotate();

        // Return true if the NEW top letter equals notch position
        return getPosition() == notchIndex;
    }

    @Override
    public int process(int index, Direction direction) {
        return (direction == Direction.FORWARD)
                ? encodeForward(index)
                : encodeBackward(index);
    }

    @Override
    public int getPosition() {
        // The top of rightColumn represents the current rotor window letter index
        return rightColumn.getFirst();
    }

    @Override
    public int getNotchInd() {
        return notchIndex;
    }

    // ---------------------------------------------------------
    // Core logic
    // ---------------------------------------------------------

    private int encodeForward(int entryIndex) {
        // right entry gives the "symbol"
        int sym = rightColumn.get(entryIndex);
        // find where this symbol appears in left column
        return leftColumn.indexOf(sym);
    }

    private int encodeBackward(int entryIndex) {
        int sym = leftColumn.get(entryIndex);
        return rightColumn.indexOf(sym);
    }

    /** Rotate both columns by shifting the first row to bottom */
    private void rotate() {
        int r0 = rightColumn.removeFirst();
        int l0 = leftColumn.removeFirst();

        rightColumn.add(r0);
        leftColumn.add(l0);

        // notch physically moves relative to top
        notchIndex = makeInBounds(notchIndex - 1);
    }

    private int makeInBounds(int x) {
        x %= alphabetSize;
        return x < 0 ? x + alphabetSize : x;
    }
}