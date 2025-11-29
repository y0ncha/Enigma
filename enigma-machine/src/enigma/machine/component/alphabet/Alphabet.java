package enigma.machine.component.alphabet;

public class Alphabet {
    private final String letters;

    public Alphabet(String letters) {
        this.letters = letters;
    }

    public int size() {
        return letters.length();
    }

    public int indexOf(char c) {
        return letters.indexOf(c);
    }

    public char charAt(int index) {
        return letters.charAt(index);
    }

    public boolean contains(char c) {
        return letters.indexOf(c) >= 0;
    }
}