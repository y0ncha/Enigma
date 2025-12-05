package enigma.machine.rotor.virtual;

import enigma.machine.alphabet.Alphabet;
import enigma.machine.rotor.Direction;
import enigma.machine.rotor.Rotor;

/**
 * Legacy rotor implementation using index-shifting (offset math) instead of
 * physical column rotation.
 *
 * <p><b>This class is deprecated.</b> Use {@link enigma.machine.rotor.RotorImpl}
 * instead, which implements the correct mechanical column-rotation model that
 * accurately reflects the physical Enigma machine behavior.</p>
 *
 * <p>This virtual rotor handles index shifting according to the rotor window
 * position using modular arithmetic. While this approach is mathematically
 * equivalent in some scenarios, it does not accurately model the physical
 * Enigma mechanism and may produce incorrect results in edge cases.</p>
 *
 * @since 1.0
 * @deprecated Use {@link enigma.machine.rotor.RotorImpl} instead. This class
 *             is retained only for reference and will be removed in a future release.
 */
@Deprecated(since = "1.0", forRemoval = true)
public class VirtualRotor implements Rotor {

    private final Alphabet alphabet;
    private final int[] forwardMapping;     // from right → left
    private final int[] backwardMapping;    // from left → right
    private final int notchInd;             // position that triggers stepping

    private int position;                   // current window index (0..alphabetSize-1)

    /**
     * Create a runtime rotor using the virtual index-shifting model.
     *
     * @param alphabet alphabet for bounds and conversions
     * @param forwardMapping forward mapping array (right→left)
     * @param backwardMapping backward mapping array (left→right)
     * @param notch notch index that triggers the next rotor step
     * @param startPosition initial rotor position (0..alphabetSize-1)
     * @since 1.0
     * @deprecated Use {@link enigma.machine.rotor.RotorImpl} constructor instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public VirtualRotor(Alphabet alphabet,
                        int[] forwardMapping,
                        int[] backwardMapping,
                        int notch,
                        int startPosition) {

        this.alphabet = alphabet;
        this.forwardMapping = forwardMapping;
        this.backwardMapping = backwardMapping;
        this.notchInd = notch;
        this.position = startPosition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean advance() {
        position = (position + 1) % alphabet.size();
        return position == notchInd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(int index, Direction direction) {
        // apply rotation offset
        int shifted = (index + position) % alphabet.size();

        int mapped = (direction == Direction.FORWARD)
                ? forwardMapping[shifted]
                : backwardMapping[shifted];

        // remove rotation offset
        return (mapped - position + alphabet.size()) % alphabet.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPosition() {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNotchInd() {
        return notchInd;
    }
}
