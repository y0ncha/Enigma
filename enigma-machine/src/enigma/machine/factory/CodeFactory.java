package enigma.machine.factory;

import enigma.machine.component.code.Code;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Factory interface for creating Code configurations.
 * 
 * <p>Implementations of this interface assemble complete Enigma code
 * configurations from rotor and reflector specifications.</p>
 */
public interface CodeFactory {
    
    /**
     * Creates a code configuration from rotors and a reflector.
     * 
     * @param rotors the list of rotors (in machine order: right to left)
     * @param reflector the reflector
     * @return a configured Code instance
     */
    Code create(List<Rotor> rotors, Reflector reflector);
}
