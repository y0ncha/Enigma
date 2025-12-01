package enigma.shared.dto.tracer;

public record ReflectorTrace(
        int entryIndex,   // index before reflector mapping
        int exitIndex,    // index after reflector mapping
        char entryChar,   // letter before reflector
        char exitChar     // letter after reflector
)
{}
