package enigma.shared.spec;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import enigma.machine.alphabet.Alphabet;

/**
 * Represents the specification of an Enigma machine, including its alphabet,
 * available rotors and reflectors.
 * This record serves as a model for configuring and describing the machine's
 * components and constraints.
 * Note: maps are stored as provided (no defensive deep copy) to preserve existing behaviour.
 *
 * @since 1.0
 */
public record MachineSpec(
        Alphabet alphabet,
        Map<Integer, RotorSpec> rotorsById,
        Map<String, ReflectorSpec> reflectorsById
) {

    /**
     * Convenience lookup for a reflector specification by its identifier.
     *
     * @param id reflector id (e.g. "I", "II")
     * @return ReflectorSpec or null if not found
     */
    public ReflectorSpec getReflectorById(String id) {
        return (reflectorsById == null) ? null : reflectorsById.get(id);
    }

    /**
     * Convenience lookup for a rotor specification by its numeric id.
     *
     * @param id rotor id
     * @return RotorSpec or null if not found
     */
    public RotorSpec getRotorById(int id) {
        return (rotorsById == null) ? null : rotorsById.get(id);
    }

    @Override
    public String toString() {
        String letters = (alphabet == null) ? "<none>" : alphabet.getLetters();
        int alphaSize = (alphabet == null) ? 0 : alphabet.size();

        String rotorsStr = "<none>";
        if (rotorsById != null && !rotorsById.isEmpty()) {
            rotorsStr = rotorsById.keySet().stream()
                    .sorted()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        String reflectorsStr = "<none>";
        if (reflectorsById != null && !reflectorsById.isEmpty()) {
            reflectorsStr = reflectorsById.keySet().stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.joining(", ", "[", "]"));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("MachineSpec:\n");
        sb.append("  Alphabet: ").append(letters).append("\n");
        sb.append("  Alphabet size: ").append(alphaSize).append("\n");
        sb.append("  Available rotors: ").append(rotorsStr).append("\n");
        sb.append("  Available reflectors: ").append(reflectorsStr).append("\n");
        return sb.toString();
    }
}
