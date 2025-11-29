package enigma.engine.components.model;

public class Alphabet {
    private final String letters; //  "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    public Alphabet(String letters) {
        this.letters = letters;
    }

    public String getLetters() {
        return letters;
    }

    public int size() {
        return letters.length();
    }

    public int indexOf(char ch) {
        return letters.indexOf(ch);
    }

    public char charAt(int index) {
        return letters.charAt(index);
    }
}
