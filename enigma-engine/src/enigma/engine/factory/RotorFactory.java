package enigma.engine.factory;

import enigma.shared.spec.RotorSpec;
import enigma.machine.rotor.Rotor;

/**
 * Factory for creating runtime {@link Rotor} instances from {@link RotorSpec}.
 *
 * <p>This factory adapts the immutable rotor specification from the XML loader
 * into a concrete runtime rotor instance. The factory is responsible for:</p>
 * <ul>
 *   <li>Building the forward and backward mappings from the specification</li>
 *   <li>Constructing the rotor with the correct notch index</li>
 *   <li>Setting the initial rotor position</li>
 * </ul>
 *
 * <p>The factory always creates {@link enigma.machine.rotor.RotorImpl} instances,
 * which use the mechanical column-rotation model that accurately reflects
 * physical Enigma behavior.</p>
 *
 * <h2>Mapping Semantics</h2>
 * <ul>
 *   <li>{@code forwardMapping}: right→left (keyboard side to reflector side)</li>
 *   <li>{@code backwardMapping}: left→right (reflector side to keyboard side)</li>
 * </ul>
 *
 * @since 1.0
 * @see enigma.machine.rotor.RotorImpl
 */
public interface RotorFactory {

    /**
     * Create a {@link Rotor} from the given specification and starting position.
     *
     * <p>The returned rotor uses the mechanical column-rotation model and
     * is set to the specified initial position.</p>
     *
     * @param spec          rotor specification containing id, notch index, and mappings
     * @param startPosition initial window position (0-based index into alphabet)
     * @return a runtime rotor instance ready for use in the machine
     */
    Rotor create(RotorSpec spec, int startPosition);
}
