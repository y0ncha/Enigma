package enigma.shared.dto.tracer;

public record RotorTrace(
        int rotorIndex,   // 0 = rightmost, 1 = next to it, ...
        int entryIndex,   // numeric index entering the rotor
        int exitIndex,    // numeric index exiting the rotor
        char entryChar,   // letter entering the rotor
        char exitChar     // letter exiting the rotor
)
{}
