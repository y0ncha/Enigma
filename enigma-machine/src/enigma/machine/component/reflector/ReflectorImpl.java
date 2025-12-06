package enigma.machine.component.reflector;

import java.lang.String;

/**
 * Simple runtime reflector using a symmetric mapping array.
 *
 * @since 1.0
 */
public class ReflectorImpl implements Reflector {

    private final int[] mapping;   // symmetric mapping array
    private final String id;

    /**
     * Create reflector with the provided alphabet and mapping.
     *
     * @param mapping symmetric mapping array (mapping[i] = j and mapping[j] = i)
     * @since 1.0
     */
    public ReflectorImpl(int[] mapping, String id) {
        this.mapping = mapping;
        this.id = id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(int index) {
        return mapping[index];
    }

    @Override
    public String getId() {
        return id;
    }

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