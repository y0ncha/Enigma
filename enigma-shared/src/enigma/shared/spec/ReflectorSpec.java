package enigma.shared.spec;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Specification for an Enigma machine reflector.
 *
 * <p>Immutable record with defensive copying for arrays to preserve immutability.
 * Use record accessor {@code id()} for the reflector identifier.</p>
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
     * @inheritDoc
     */
    @Override
    public int[] mapping() {
        return mapping.clone();
    }

    private String mappingToString() {
        return IntStream.range(0, mapping.length)
                .mapToObj(i -> (char)('A' + i) + "->" + (char)('A' + mapping[i]))
                .collect(Collectors.joining(", "));
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
        return String.format("Reflector ID: %s, Mapping: [%s]", id, mappingToString());
    }
}
