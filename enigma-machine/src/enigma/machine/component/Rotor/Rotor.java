package enigma.machine.component.rotor;

public interface Rotor {
    int process(int input, Direction direction);
    boolean advance();
}
