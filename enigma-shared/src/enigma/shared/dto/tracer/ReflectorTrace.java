package enigma.shared.dto.tracer;

/**
 * Trace of a signal passing through the reflector.
 *
 * @param entryIndex index before reflector mapping
 * @param exitIndex index after reflector mapping
 * @param entryChar letter before reflector
 * @param exitChar letter after reflector
 * @since 1.0
 */
public record ReflectorTrace(
        int entryIndex,   // index before reflector mapping
        int exitIndex,    // index after reflector mapping
        char entryChar,   // letter before reflector
        char exitChar     // letter after reflector
)
{}
