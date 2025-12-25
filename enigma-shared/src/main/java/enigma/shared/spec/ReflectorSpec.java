package enigma.shared.spec;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Specification for an Enigma machine reflector.
 *
 * <p><b>Module:</b> enigma-shared (specs)</p>
 *
 * <p>Immutable record with defensive copying for arrays to preserve immutability.</p>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li><b>id:</b> Reflector identifier (Roman numeral: "I", "II", "III", ...)</li>
 *   <li><b>mapping:</b> Symmetric int array where mapping[i] = j and mapping[j] = i</li>
 * </ul>
 *
 * <h2>Mapping Semantics</h2>
 * <p>The mapping array defines symmetric pairwise transformations:</p>
 * <ul>
 *   <li>If mapping[i] = j, then mapping[j] = i (loader validates this)</li>
 *   <li>All indices [0, alphabetSize) are covered</li>
 *   <li>No self-mapping: mapping[i] ≠ i</li>
 * </ul>
 *
 * <p><b>Wiring Order:</b> The mapping is constructed directly from XML pairs
 * without reordering. Loader preserves XML-defined pair relationships exactly.</p>
 *
 * <p><b>Note:</b> Use record accessor {@code id()} for the reflector identifier.
 * The {@code mapping()} accessor returns a defensive copy.</p>
 *
 * @param id reflector identifier (e.g., "I", "II")
 * @param mapping symmetric mapping array (mapping[i] = j ⟺ mapping[j] = i)
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
     * Return a defensive copy of the symmetric mapping array.
     *
     * @return cloned mapping array
     */
    @Override
    public int[] mapping() {
        return mapping.clone();
    }

    /**
     * Build a string representation of the mapping (index→index pairs).
     *
     * <p>Format: "A→B, B→A, C→D, D→C, ..." showing symmetric pairs.</p>
     *
     * @return comma-separated mapping pairs
     */
    private String mappingToString() {
        return IntStream.range(0, mapping.length)
                .mapToObj(i -> (char)('A' + i) + "->" + (char)('A' + mapping[i]))
                .collect(Collectors.joining(", "));
    }

    /**
     * Generate a string representation of the reflector spec.
     *
     * <p>Format: "Reflector ID: {id}, Mapping: [index→index pairs]"</p>
     *
     * @return formatted reflector spec string
     */
    @Override
    public String toString() {
        return String.format("Reflector ID: %s, Mapping: [%s]", id, mappingToString());
    }
}
