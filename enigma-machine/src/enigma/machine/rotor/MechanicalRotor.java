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

    private final int notchIndex; // which row triggers stepping of next rotor
    private final int id;

    /**
     * Construct a rotor from forward mapping and notch index.
     *
     * @param forwardMapping mapping from right→left (base wiring)
     * @param notchIndex index at which the rotor triggers stepping (0..N-1)
     * @param alphabetSize size of alphabet
     */
    public MechanicalRotor(int[] forwardMapping, int[] backwardMapping, int notchIndex, int alphabetSize, int id) {
        this.alphabetSize = alphabetSize;
        // Build columns
        this.rightColumn = new ArrayList<>(alphabetSize);
        this.leftColumn  = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            rightColumn.add(i);          // fixed identity column (A,B,C,...)
        }
        for (int i = 0; i < alphabetSize; i++) {
            leftColumn.add(forwardMapping[i]);
        }


        // Store notch
        this.notchIndex = makeInBounds(notchIndex);
        this.id = id;
    }

    // ---------------------------------------------------------
    // Interface compliance
    // ---------------------------------------------------------

    @Override
    public boolean advance() {
        rotate();

        // Return true if the NEW top letter equals notch position
        int pos = getPosition();
        return pos == notchIndex;
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

    public void setPosition(int pos) {
        pos = makeInBounds(pos);
        int safety = alphabetSize;
        while (getPosition() != pos && safety-- > 0) {
            rotate();
        }
        if (getPosition() != pos) {
            throw new IllegalStateException("Failed to reach position " + pos + " in MechanicalRotor");
        }
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
        int index = leftColumn.indexOf(sym);
        return index;
    }

    private int encodeBackward(int entryIndex) {
        int sym = leftColumn.get(entryIndex);
        int index = rightColumn.indexOf(sym);
        return index;
    }

    /** Rotate both columns by shifting the first row to bottom */
    private void rotate() {
        int r0 = rightColumn.removeFirst();
        int l0 = leftColumn.removeFirst();

        rightColumn.add(r0);
        leftColumn.add(l0);
    }

    private int makeInBounds(int x) {
        x %= alphabetSize;
        return x < 0 ? x + alphabetSize : x;
    }
}