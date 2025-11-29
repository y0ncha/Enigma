package enigma.machine.component.code;

import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

public interface Code {

    // Components
    List<Rotor> getRotors();
    Reflector getReflector();

    // Metadata
    List<Integer> getPositions();
    List<Integer> getRotorIds();
    String getReflectorId();
}
