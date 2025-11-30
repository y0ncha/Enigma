package enigma.engine.components.model;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;
import enigma.machine.component.alphabet.Alphabet;

/**
 * Represents the specification of an Enigma machine, including its alphabet,
 * available rotors and reflectors, and the number of rotors used in operation.
 * This class serves as a model for configuring and describing the machine's
 * components and constraints.
 *
 * @since 1.0
 */
public class MachineSpec {
    private final Alphabet alphabet;
    private final Map<Integer, RotorSpec> rotorsById;
    private final Map<String, ReflectorSpec> reflectorsById;
    private final int rotorsCountInUse; // Used in exercise 2+

    public MachineSpec(Alphabet alphabet,
                       Map<Integer, RotorSpec> rotorsById,
                       Map<String, ReflectorSpec> reflectorsById,
                       int rotorsCountInUse) {
        this.alphabet = alphabet;
        this.rotorsById = rotorsById;
        this.reflectorsById = reflectorsById;
        this.rotorsCountInUse = rotorsCountInUse;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public Map<Integer, RotorSpec> getRotorsById() {
        return rotorsById;
    }

    public Map<String, ReflectorSpec> getReflectorsById() {
        return reflectorsById;
    }

    public int getRotorsCountInUse() {
        return rotorsCountInUse;
    }

    // todo delete
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
        sb.append("  Rotors in use: ").append(rotorsCountInUse).append("\n");
        sb.append("  Available rotors: ").append(rotorsStr).append("\n");
        sb.append("  Available reflectors: ").append(reflectorsStr).append("\n");
        return sb.toString();
    }

}
