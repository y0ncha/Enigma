package enigma.machine.component.rotor.virtual;

import enigma.machine.component.rotor.Direction;
import enigma.machine.component.rotor.Rotor;

/**
 * Virtual (index-shifting) implementation of the Enigma rotor.
 * 
 * <p><strong>DEPRECATED:</strong> This implementation uses mathematical index shifting
 * to simulate rotor behavior, which is confusing and does not accurately model
 * the physical Enigma machine. Use {@link enigma.machine.component.rotor.RotorImpl}
 * instead, which implements the mechanical model with left/right columns.</p>
 * 
 * <p>This class is retained for reference and backward compatibility only.
 * It should not be used in production code.</p>
 * 
 * <p><strong>How it works:</strong></p>
 * <p>Instead of physically rotating columns, this implementation uses offset
 * arithmetic: {@code (index + position) % size} to compute the effective
 * position within the wiring arrays. The forward and backward arrays store
 * the static wiring mappings.</p>
 * 
 * @deprecated Use {@link enigma.machine.component.rotor.RotorImpl} instead.
 *             The mechanical model provides a clearer, more accurate representation
 *             of actual Enigma rotor behavior.
 */
@Deprecated
public class VirtualRotorImpl implements Rotor {
    
    private final int[] forwardMapping;
    private final int[] backwardMapping;
    private final int notchPosition;
    private int position;
    private final int alphabetSize;
    
    /**
     * Constructs a virtual rotor with the specified wiring mappings.
     * 
     * @param forwardMapping the wiring for forward (right→left) signal flow
     * @param backwardMapping the wiring for backward (left→right) signal flow
     * @param notchPosition the position where the notch engages (triggers next rotor)
     * @deprecated Use {@link enigma.machine.component.rotor.RotorImpl} instead.
     */
    @Deprecated
    public VirtualRotorImpl(int[] forwardMapping, int[] backwardMapping, int notchPosition) {
        this.forwardMapping = forwardMapping.clone();
        this.backwardMapping = backwardMapping.clone();
        this.notchPosition = notchPosition;
        this.alphabetSize = forwardMapping.length;
        this.position = 0;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Uses index-shifting arithmetic to compute the output:
     * {@code output = (mapping[(input + position) % size] - position + size) % size}</p>
     * 
     * @deprecated This method uses confusing offset math. Use the mechanical
     *             {@link enigma.machine.component.rotor.RotorImpl} instead.
     */
    @Override
    @Deprecated
    public int process(int input, Direction direction) {
        int[] mapping = (direction == Direction.FORWARD) ? forwardMapping : backwardMapping;
        int shiftedInput = (input + position) % alphabetSize;
        int mappedValue = mapping[shiftedInput];
        return (mappedValue - position + alphabetSize) % alphabetSize;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Increments the position counter. The notch triggers when position
     * equals the notch position.</p>
     * 
     * @deprecated Use the mechanical {@link enigma.machine.component.rotor.RotorImpl}
     *             which physically rotates its columns.
     */
    @Override
    @Deprecated
    public boolean advance() {
        boolean notchEngaged = (position == notchPosition);
        position = (position + 1) % alphabetSize;
        return notchEngaged;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use the mechanical {@link enigma.machine.component.rotor.RotorImpl}.
     */
    @Override
    @Deprecated
    public int getPosition() {
        return position;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use the mechanical {@link enigma.machine.component.rotor.RotorImpl}.
     */
    @Override
    @Deprecated
    public int getNotchInd() {
        return notchPosition;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @deprecated Use the mechanical {@link enigma.machine.component.rotor.RotorImpl}.
     */
    @Override
    @Deprecated
    public void setPosition(int position) {
        this.position = position % alphabetSize;
    }
}
