package enigma.machine.component.Code;

import enigma.machine.component.Reflector.Reflector;
import enigma.machine.component.Rotor.Rotor;

import java.util.List;

public interface Code {
    List<Rotor> getRotors();
    Reflector getReflector();
    List<Integer> getPositions();
}
