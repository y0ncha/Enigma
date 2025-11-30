package enigma.engine.components.model;

/**
 * Specification for an Enigma machine reflector.
 * <p>
 * A reflector is a fixed component that maps each input position to another position,
 * ensuring the mapping is symmetric (i.e., if A maps to B, then B maps to A).
 * This class holds the reflector's identifier (e.g., "I", "II") and its wiring mapping.
 */
public class ReflectorSpec {
    private final String id; // "I", "II", ...
    private final int[] mapping; // mapping[i] = j ; symmetric

    public ReflectorSpec(String id, int[] mapping) {
        this.id = id;
        this.mapping = mapping;
    }

    public String getId() {
        return id;
    }

    public int[] getMapping() {
        return mapping.clone();
    }
}
