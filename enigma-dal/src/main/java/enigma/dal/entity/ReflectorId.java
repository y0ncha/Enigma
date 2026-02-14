package enigma.dal.entity;

public enum ReflectorId {
    I,
    II,
    III,
    IV,
    V;

    public static ReflectorId fromDbValue(String value) {
        return ReflectorId.valueOf(value.trim().toUpperCase());
    }

    public String toDbValue() {
        return name();
    }
}
