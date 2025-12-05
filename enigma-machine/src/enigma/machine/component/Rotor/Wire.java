package enigma.machine.component.rotor;

/**
 * Represents a single wire connection in a rotor.
 * 
 * <p>Each wire connects a contact on the right side (entry) of the rotor
 * to a contact on the left side (exit). This immutable record ensures
 * that the paired values stay coupled and prevents accidental desynchronization.</p>
 * 
 * <p><strong>Physical Model:</strong></p>
 * <pre>
 *   Right (Entry)  ────────→  Left (Exit)
 *        0         ────────→      4
 *        1         ────────→      2
 *        2         ────────→      0
 * </pre>
 * 
 * @param right the right-side contact (entry point for forward signal)
 * @param left the left-side contact (exit point for forward signal)
 */
public record Wire(int right, int left) {
}
