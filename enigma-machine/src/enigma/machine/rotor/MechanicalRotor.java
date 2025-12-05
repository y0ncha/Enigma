package enigma.machine.rotor;

/**
 * Legacy name for the mechanical rotor implementation.
 *
 * <p><b>This class is deprecated.</b> Use {@link RotorImpl} directly instead.
 * This class extends {@link RotorImpl} and provides identical functionality
 * under the old name for backward compatibility.</p>
 *
 * @since 1.0
 * @deprecated Use {@link RotorImpl} instead. This class is retained only for
 *             backward compatibility and will be removed in a future release.
 */
@Deprecated(since = "1.0", forRemoval = true)
public class MechanicalRotor extends RotorImpl {

    /**
     * Construct a rotor from forward mapping and notch index.
     *
     * @param forwardMapping mapping from right→left (base wiring)
     * @param backwardMapping inverse mapping (left→right)
     * @param notchIndex index at which the rotor triggers stepping (0..N-1)
     * @param alphabetSize size of alphabet
     * @param id rotor identifier
     * @deprecated Use {@link RotorImpl#RotorImpl(int[], int[], int, int, int)} instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public MechanicalRotor(int[] forwardMapping, int[] backwardMapping, int notchIndex, int alphabetSize, int id) {
        super(forwardMapping, backwardMapping, notchIndex, alphabetSize, id);
    }
}