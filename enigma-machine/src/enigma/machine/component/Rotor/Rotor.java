package enigma.machine.component.Rotor;

public interface Rotor {
    int process(int input, Direction direction);
    boolean advance();
}
