package enigma.machine.component.rotor;

/**
 * Wire pair representing a single row inside a mechanical rotor.
 *
 * <p>Holds the right-side (keyboard-facing) and left-side (reflector-facing)
 * contact characters for that row. This value type is used by {@link RotorImpl}
 * to model the physical rotor wiring.</p>
 *
 * <p><b>Mechanical Model:</b></p>
 * <ul>
 *   <li>right: character on the keyboard-facing (entry) side</li>
 *   <li>left: character on the reflector-facing (exit) side</li>
 * </ul>
 *
 * @param right keyboard-facing contact character
 * @param left reflector-facing contact character
 */
public record Wire(char right, char left) {}
