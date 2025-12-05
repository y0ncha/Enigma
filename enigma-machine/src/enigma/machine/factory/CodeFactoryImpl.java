package enigma.machine.factory;

import enigma.machine.component.code.Code;
import enigma.machine.component.code.CodeImpl;
import enigma.machine.component.reflector.Reflector;
import enigma.machine.component.rotor.Rotor;

import java.util.List;

/**
 * Factory implementation for creating Code configurations.
 * 
 * <p>This factory assembles complete Enigma code configurations by combining
 * rotors (in machine order) with a reflector. The factory ensures proper
 * ordering and configuration of components.</p>
 * 
 * <h2>Factory Responsibilities:</h2>
 * <ol>
 *   <li>Accept rotors in left→right order from configuration</li>
 *   <li>Request each rotor from the {@link RotorFactory}</li>
 *   <li>Build reflector from reflector specification</li>
 *   <li>Return a fully configured {@link Code}</li>
 * </ol>
 * 
 * <h2>Rotor Ordering:</h2>
 * <p>Rotors should be provided in machine order (right to left, where index 0
 * is the rightmost/fastest rotor). This matches the signal flow direction
 * from keyboard to reflector.</p>
 * 
 * <h2>Usage with RotorFactory:</h2>
 * <p>Typically, this factory is used in conjunction with {@link RotorFactoryImpl}
 * to first create individual rotors from specifications, then assemble them
 * into a complete code configuration:</p>
 * <pre>{@code
 * RotorFactory rotorFactory = new RotorFactoryImpl();
 * CodeFactory codeFactory = new CodeFactoryImpl();
 * 
 * // Create rotors from specs
 * Rotor rotor1 = rotorFactory.create(rightCol1, leftCol1, notch1, pos1);
 * Rotor rotor2 = rotorFactory.create(rightCol2, leftCol2, notch2, pos2);
 * Rotor rotor3 = rotorFactory.create(rightCol3, leftCol3, notch3, pos3);
 * 
 * // Create reflector
 * Reflector reflector = new ReflectorImpl(reflectorMapping);
 * 
 * // Assemble code (rotors in right-to-left order)
 * Code code = codeFactory.create(List.of(rotor1, rotor2, rotor3), reflector);
 * }</pre>
 */
public class CodeFactoryImpl implements CodeFactory {
    
    /**
     * Creates a code configuration from the provided rotors and reflector.
     * 
     * <p>The rotors should be in machine order (right to left, index 0 = rightmost).
     * The factory simply assembles these components into a {@link CodeImpl}.</p>
     * 
     * @param rotors the list of rotors in machine order
     * @param reflector the reflector
     * @return a configured Code instance ready for use in the machine
     */
    @Override
    public Code create(List<Rotor> rotors, Reflector reflector) {
        return new CodeImpl(rotors, reflector);
    }
}
