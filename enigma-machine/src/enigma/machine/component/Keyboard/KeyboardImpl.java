package enigma.machine.component.keyboard;

import enigma.machine.component.alphabet.Alphabet;

public class KeyboardImpl implements Keyboard {

    private final Alphabet alphabet;

    public KeyboardImpl(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    @Override
    public int process(char input) {
        int index = alphabet.indexOf(input);
        if (index < 0) {
            throw new IllegalArgumentException("Invalid character for this machine: " + input);
        }
        return index;
    }

    @Override
    public char lightKey(int input) {
        return alphabet.charAt(input);
    }
}