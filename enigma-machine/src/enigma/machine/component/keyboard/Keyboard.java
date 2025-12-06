package enigma.machine.component.keyboard;

/**
 * Keyboard adapter that converts between characters and internal indices.
 * Implementations should validate input characters against the machine alphabet.
 *
 * @since 1.0
 */
public interface Keyboard {

    char toChar(int idx);

    int toIdx(char ch);

    boolean charInbound(char ch);

    boolean idxInbound(int idx);

    int size();
}
