package enigma.machine.rotor;

import enigma.machine.alphabet.Alphabet;

/**
 * Runtime rotor created from rotor mappings and a starting position.
 * Handles index shifting according to the rotor window position.
 *
 * @since 1.0
 */
public class RotorImpl implements Rotor {

    private final Alphabet alphabet;
    private final int[] forwardMapping;     // from right → left
    private final int[] backwardMapping;    // from left → right
    private final int notchInd;                // position that triggers stepping

    private int position;                   // current window index (0..alphabetSize-1)

    /**
     * Create a runtime rotor.
     *
     * @param alphabet alphabet for bounds and conversions
     * @param forwardMapping forward mapping array (right→left)
     * @param backwardMapping backward mapping array (left→right)
     * @param notch notch index that triggers the next rotor step
     * @param startPosition initial rotor position (0..alphabetSize-1)
     * @since 1.0
     */
    public RotorImpl(Alphabet alphabet,
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