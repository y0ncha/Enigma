package enigma.machine.component.reflector;

import enigma.machine.component.alphabet.Alphabet;

public class ReflectorImpl implements Reflector {

    private final Alphabet alphabet;
    private final int[] mapping;   // symmetric mapping array

    // todo - change after understanding hows th code is being generated from the xml and user input, maybe builder or factory design pattern
    public ReflectorImpl(Alphabet alphabet, int[] mapping) {
        this.alphabet = alphabet;
        this.mapping = mapping;
    }

    @Override
    public int process(int index) {
        return mapping[index];
    }
}