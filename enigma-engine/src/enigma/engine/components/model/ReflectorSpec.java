package enigma.engine.components.model;

import java.util.Objects;

/**
 * Specification for an Enigma machine reflector.
 *
 * <p>Immutable record with defensive copying for arrays to preserve immutability.</p>
 *
 * @since 1.0
 */
public record ReflectorSpec(
        String id,
        int[] mapping
) {
    /**
     * Canonical constructor with validation and defensive copy.
     *
     * @param id reflector identifier
     * @param mapping symmetric mapping array
     */
    public ReflectorSpec {
        Objects.requireNonNull(id);
        Objects.requireNonNull(mapping);
        mapping = mapping.clone();
    }

    /**
     * Return the reflector identifier.
     *
     * @return reflector id string
     */
    public String getId() {
        return id;
    }

    /**
     * Return a defensive copy of the mapping array.
     *
     * @return copy of the mapping array
     */
    public int[] getMapping() {
        return mapping.clone();
    }
}
