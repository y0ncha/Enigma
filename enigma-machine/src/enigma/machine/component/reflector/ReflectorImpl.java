package enigma.machine.component.reflector;

import java.lang.String;

/**
 * Simple runtime reflector using a symmetric mapping array.
 *
 * <p><b>Module:</b> enigma-machine</p>
 *
 * <h2>Mechanical Model</h2>
 * <p>The reflector implements a symmetric pairwise mapping where
 * if mapping[i] = j, then mapping[j] = i. Each index is paired
 * with exactly one other index (or itself, though this is rare).</p>
 *
 * <h2>Wiring Order</h2>
 * <p>The mapping array is constructed directly from XML-defined wiring
 * without reordering. The loader validates symmetry and bijectivity.</p>
 *
 * <h2>Display Format (toString)</h2>
 * <p>Each row shows a pair label computed as min(i, partner) + 1.
 * This ensures each pair gets a unique 1-based label, and both
 * indices in a pair display the same label.</p>
 *
 * <h2>Invariants</h2>
 * <ul>
 *   <li>mapping.length = alphabetSize</li>
 *   <li>∀i: mapping[mapping[i]] = i (symmetric)</li>
 *   <li>All indices in [0, alphabetSize) are covered</li>
 * </ul>
 *
 * @since 1.0
 */
public class ReflectorImpl implements Reflector {

    private final int[] mapping;   // symmetric mapping array
    private final String id;

    /**
     * Create reflector with the provided mapping and identifier.
     *
     * <p>The mapping array must be symmetric: mapping[i] = j implies mapping[j] = i.
     * Validation is performed by the loader/factory.</p>
     *
     * @param mapping symmetric mapping array (mapping[i] = j and mapping[j] = i)
     * @param id reflector identifier (e.g., "I", "II")
     * @since 1.0
     */
    public ReflectorImpl(int[] mapping, String id) {
        this.mapping = mapping;
        this.id = id;
    }

    /**
     * Transform the input index through the reflector's symmetric mapping.
     *
     * @param index input index (0..alphabetSize-1)
     * @return paired index via symmetric mapping
     */
    @Override
    public int process(int index) {
        return mapping[index];
    }

    /**
     * Get the reflector's identifier.
     *
     * @return reflector ID (e.g., "I", "II")
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Generate a visual column representation of the reflector wiring.
     *
     * <p>Each row displays a pair label computed as min(i, partner) + 1,
     * ensuring both indices in a symmetric pair show the same label.</p>
     *
     * @return multi-line string column for wiring display
     */
    @Override
    public String toString() {
        final int colInner = 9;
        StringBuilder sb = new StringBuilder();

        java.util.function.BiFunction<String,Integer,String> center = (s,w) -> {
            if (s == null) s = "";
            if (s.length() >= w) return s.substring(0,w);
            int left = (w - s.length())/2;
            int right = w - s.length() - left;
            return " ".repeat(left) + s + " ".repeat(right);
        };

        // small ID box
        sb.append("  ┌").append("─".repeat(colInner)).append("┐\n");
        sb.append("  │").append(center.apply("Ref " + id, colInner)).append("│\n");
        sb.append("  └").append("─".repeat(colInner)).append("┘\n");

        // main tall box top
        sb.append("  ┌").append("─".repeat(colInner)).append("┐\n");

        // data rows: show pair label (min(i, partner)+1)
        for (int i = 0; i < mapping.length; i++) {
            int partner = mapping[i];
            int pairLabel = Math.min(i, partner) + 1; // 1-based
            sb.append("  │").append(center.apply(String.valueOf(pairLabel), colInner)).append("│\n");
        }

        // bottom
        sb.append("  └").append("─".repeat(colInner)).append("┘\n");

        return sb.toString();
    }


}