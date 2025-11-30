package enigma.engine.components.model;

public class ReflectorSpecification {
    private final String id; // "I", "II", ...
    private final int[] mapping; // mapping[i] = j ; symmetric

    public ReflectorSpecification(String id, int[] mapping) {
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
