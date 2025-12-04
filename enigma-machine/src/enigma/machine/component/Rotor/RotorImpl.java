package enigma.machine.component.rotor;

import java.util.ArrayList;
import java.util.List;

/**
 * Mechanical implementation of the Enigma rotor.
 * 
 * <p>This implementation models the physical wiring of a real Enigma rotor using
 * two columns: a right column and a left column. Each row represents a wire
 * connecting a contact on the right side to a contact on the left side.</p>
 * 
 * <p><strong>Physical Model:</strong></p>
 * <pre>
 *   Right Column    Left Column
 *   (Entry Side)    (Exit Side)
 *       0     ────────→  4
 *       1     ────────→  2
 *       2     ────────→  0
 *       3     ────────→  1
 *       4     ────────→  3
 * </pre>
 * 
 * <p><strong>Rotation Mechanism:</strong></p>
 * <p>When the rotor advances (rotates), the first row moves to the bottom,
 * simulating the physical rotation of the rotor wheel:</p>
 * <pre>
 *   Before:    After advance():
 *     0→4         1→2
 *     1→2         2→0
 *     2→0    →    3→1
 *     3→1         4→3
 *     4→3         0→4
 * </pre>
 * 
 * <p><strong>Signal Processing:</strong></p>
 * <ul>
 *   <li>FORWARD (right→left): Find the row where rightColumn equals input,
 *       return the corresponding leftColumn value.</li>
 *   <li>BACKWARD (left→right): Find the row where leftColumn equals input,
 *       return the corresponding rightColumn value.</li>
 * </ul>
 * 
 * <p><strong>Position:</strong></p>
 * <p>The position is indicated by the value in rightColumn.get(0), representing
 * the letter visible through the machine's window.</p>
 * 
 * <p><strong>Notch:</strong></p>
 * <p>The notch position is tracked and updated during rotation. When the notch
 * reaches the window (position 0 in the internal representation), it triggers
 * the next rotor to advance.</p>
 */
public class RotorImpl implements Rotor {
    
    private final List<Integer> rightColumn;
    private final List<Integer> leftColumn;
    private int notchIndex;
    private final int alphabetSize;
    
    /**
     * Constructs a mechanical rotor with the specified wiring and notch position.
     * 
     * @param rightColumn the right column values (entry contacts), representing
     *                    the order of alphabet indices from top to bottom
     * @param leftColumn the left column values (exit contacts), where leftColumn[i]
     *                   is wired to rightColumn[i]
     * @param notchIndex the initial notch index (position where notch triggers next rotor)
     */
    public RotorImpl(List<Integer> rightColumn, List<Integer> leftColumn, int notchIndex) {
        this.rightColumn = new ArrayList<>(rightColumn);
        this.leftColumn = new ArrayList<>(leftColumn);
        this.notchIndex = notchIndex;
        this.alphabetSize = rightColumn.size();
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>For FORWARD direction: finds the row where rightColumn equals input,
     * returns the corresponding leftColumn value (right→left mapping).</p>
     * 
     * <p>For BACKWARD direction: finds the row where leftColumn equals input,
     * returns the corresponding rightColumn value (left→right mapping).</p>
     */
    @Override
    public int process(int input, Direction direction) {
        if (direction == Direction.FORWARD) {
            // Forward: find input in right column, return corresponding left column value
            for (int i = 0; i < alphabetSize; i++) {
                if (rightColumn.get(i) == input) {
                    return leftColumn.get(i);
                }
            }
        } else {
            // Backward: find input in left column, return corresponding right column value
            for (int i = 0; i < alphabetSize; i++) {
                if (leftColumn.get(i) == input) {
                    return rightColumn.get(i);
                }
            }
        }
        // Should never reach here with valid input
        throw new IllegalArgumentException("Invalid input index: " + input);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Rotation is performed by moving the first element of both columns
     * to the end, simulating the physical rotation of the rotor wheel.</p>
     * 
     * <p>The notch index is also updated: it decrements by 1, wrapping around
     * when it reaches -1 to become (alphabetSize - 1).</p>
     */
    @Override
    public boolean advance() {
        // Check if notch is at window position (index 0) before rotation
        boolean notchAtWindow = (notchIndex == 0);
        
        // Rotate: move first element to the end for both columns
        rightColumn.add(rightColumn.remove(0));
        leftColumn.add(leftColumn.remove(0));
        
        // Update notch index (it moves up one position relative to window)
        notchIndex = (notchIndex - 1 + alphabetSize) % alphabetSize;
        
        return notchAtWindow;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Returns the value at the top of the right column, which represents
     * the letter currently visible in the machine's window.</p>
     */
    @Override
    public int getPosition() {
        return rightColumn.get(0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getNotchInd() {
        return notchIndex;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Rotates the rotor until the window letter (rightColumn.get(0))
     * equals the specified position.</p>
     */
    @Override
    public void setPosition(int position) {
        while (getPosition() != position) {
            advance();
        }
    }
}
