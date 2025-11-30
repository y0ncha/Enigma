package enigma.engine.components.model;

import java.util.Objects;

/**
 * Specification for an Enigma machine reflector.
 * Immutable record with defensive copying for arrays.
 */
public record ReflectorSpec(
        String id,
        int[] mapping
) {
    public ReflectorSpec {
        Objects.requireNonNull(id);
        Objects.requireNonNull(mapping);
        mapping = mapping.clone();
    }

    public String getId() {
        return id;
    }

    public int[] getMapping() {
        return mapping.clone();
    }
}
