package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

public class CodeImpl implements Code {

    // active components
    private final List<Rotor> rotors;        // right → left
    private final Reflector reflector;

    // metadata (config)
    private final List<Integer> rotorIds;    // right → left
    private final List<Integer> positions;   // numeric positions (0..|ABC|-1)
    private final String reflectorId;        // "I", "II", ...

    // todo - change after understanding hows th code is being generated from the xml and user input, maybe builder or factory design pattern
    public CodeImpl(List<Rotor> rotors,
                    Reflector reflector,
                    List<Integer> rotorIds,
                    List<Integer> positions,
                    String reflectorId) {

        this.rotors = List.copyOf(rotors);
        this.reflector = reflector;
        this.rotorIds = List.copyOf(rotorIds);
        this.positions = List.copyOf(positions);
        this.reflectorId = reflectorId;
    }

    @Override
    public List<Rotor> getRotors() {
        return rotors;
    }

    @Override
    public Reflector getReflector() {
        return reflector;
    }

    @Override
    public List<Integer> getRotorIds() {
        return rotorIds;
    }

    @Override
    public List<Integer> getPositions() {
        return positions;
    }

    @Override
    public String getReflectorId() {
        return reflectorId;
    }
}