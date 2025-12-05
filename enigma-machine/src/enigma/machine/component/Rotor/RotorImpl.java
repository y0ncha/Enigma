package enigma.machine.component.rotor;

import java.util.ArrayList;
import java.util.List;

/**
 * Mechanical implementation of the Enigma rotor.
 * 
 * <p>This implementation models the physical wiring of a real Enigma rotor using
 * a list of {@link Wire} pairs. Each wire represents a connection between a
 * contact on the right side (entry) and a contact on the left side (exit).
 * Using paired values ensures the rotor's two columns always stay coupled and
 * prevents accidental desynchronization.</p>
 * 
 * <p><strong>Physical Model:</strong></p>
 * <pre>
 *   Wire List (right → left)
 *       0     ────────→  4
 *       1     ────────→  2
 *       2     ────────→  0
 *       3     ────────→  1
 *       4     ────────→  3
 * </pre>
 * 
 * <p><strong>Rotation Mechanism:</strong></p>
 * <p>When the rotor advances (rotates), the first wire moves to the bottom,
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
 *   <li>FORWARD (right→left): Find the wire whose {@code right} equals input,
 *       return its {@code left} value.</li>
 *   <li>BACKWARD (left→right): Find the wire whose {@code left} equals input,
 *       return its {@code right} value.</li>
 * </ul>
 * 
 * <p><strong>Position:</strong></p>
 * <p>The position is indicated by the value in {@code wires.get(0).right()},
 * representing the letter visible through the machine's window.</p>
 * 
 * <p><strong>Notch:</strong></p>
 * <p>The notch position is tracked and updated during rotation. When the notch
 * reaches the window (position 0 in the internal representation), it triggers
 * the next rotor to advance.</p>
 */
public class RotorImpl implements Rotor {
    
    private final List<Wire> wires;
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
        this.wires = new ArrayList<>();
        for (int i = 0; i < rightColumn.size(); i++) {
            wires.add(new Wire(rightColumn.get(i), leftColumn.get(i)));
        }
        this.notchIndex = notchIndex;
        this.alphabetSize = wires.size();
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>For FORWARD direction: finds the wire whose {@code right} equals input,
     * returns its {@code left} value (right→left mapping).</p>
     * 
     * <p>For BACKWARD direction: finds the wire whose {@code left} equals input,
     * returns its {@code right} value (left→right mapping).</p>
     */
    @Override
    public int process(int input, Direction direction) {
        if (direction == Direction.FORWARD) {
            // Forward: find wire with matching right value, return its left value
            for (int i = 0; i < alphabetSize; i++) {
                if (wires.get(i).right() == input) {
                    return wires.get(i).left();
                }
            }
        } else {
            // Backward: find wire with matching left value, return its right value
            for (int i = 0; i < alphabetSize; i++) {
                if (wires.get(i).left() == input) {
                    return wires.get(i).right();
                }
            }
        }
        // Should never reach here with valid input
        throw new IllegalArgumentException("Invalid input index: " + input);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Rotation is performed by moving the first wire to the end of the list,
     * simulating the physical rotation of the rotor wheel.</p>
     * 
     * <p>The notch index is also updated: it decrements by 1, wrapping around
     * when it reaches -1 to become (alphabetSize - 1).</p>
     */
    @Override
    public boolean advance() {
        // Check if notch is at window position (index 0) before rotation
        boolean notchAtWindow = (notchIndex == 0);
        
        // Rotate: move first wire to the end
        wires.add(wires.remove(0));
        
        // Update notch index (it moves up one position relative to window)
        notchIndex = (notchIndex - 1 + alphabetSize) % alphabetSize;
        
        return notchAtWindow;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Returns the {@code right} value of the top wire, which represents
     * the letter currently visible in the machine's window.</p>
     */
    @Override
    public int getPosition() {
        return wires.get(0).right();
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
     * <p>Rotates the rotor until the window letter ({@code wires.get(0).right()})
     * equals the specified position.</p>
     */
    @Override
    public void setPosition(int position) {
        while (getPosition() != position) {
            advance();
        }
    }
}
