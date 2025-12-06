package enigma.machine.component.rotor;

/**
 * Wire pair representing a single row inside a mechanical rotor.
 *
 * <p>Holds the right-side (keyboard-facing) and left-side
 * (reflector-facing) contact indices for that row.</p>
 *
 * This is a small value type intended to be used by {@link RotorImpl}.
 */
public record Wire(int right, int left) {}
