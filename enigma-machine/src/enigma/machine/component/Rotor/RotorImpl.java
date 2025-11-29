package enigma.machine.component.rotor;

import enigma.machine.component.alphabet.Alphabet;

/**
 * Runtime rotor created from a RotorDefinition and a starting position.
 */
public class RotorImpl implements Rotor {

    private final Alphabet alphabet;
    private final int[] forwardMapping;     // from right → left
    private final int[] backwardMapping;    // from left → right
    private final int notch;                // position that triggers stepping

    private int position;                   // current window index (0..alphabetSize-1)

    // todo - change after understanding hows th code is being generated from the xml and user input, maybe builder or factory design pattern
    public RotorImpl(Alphabet alphabet,
                     int[] forwardMapping,
                     int[] backwardMapping,
                     int notch,
                     int startPosition) {

        this.alphabet = alphabet;
        this.forwardMapping = forwardMapping;
        this.backwardMapping = backwardMapping;
        this.notch = notch;
        this.position = startPosition;
    }

    @Override
    public boolean advance() {
        position = (position + 1) % alphabet.size();
        return position == notch;
    }

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

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position % alphabet.size();
    }

    @Override
    public int getNotch() {
        return notch;
    }
}