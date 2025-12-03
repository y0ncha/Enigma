package enigma.shared.dto.tracer;

/**
 * Trace of a signal passing through a single rotor.
 *
 * @param rotorIndex rotor position in left→right order (0 = leftmost)
 * @param entryIndex numeric index entering the rotor
 * @param exitIndex numeric index exiting the rotor
 * @param entryChar letter entering the rotor
 * @param exitChar letter exiting the rotor
 * @since 1.0
 */
public record RotorTrace(
        int rotorIndex,   // 0 = leftmost, 1 = next to it, ..., last = rightmost
        int entryIndex,   // numeric index entering the rotor
        int exitIndex,    // numeric index exiting the rotor
        char entryChar,   // letter entering the rotor
        char exitChar     // letter exiting the rotor
)
{}
