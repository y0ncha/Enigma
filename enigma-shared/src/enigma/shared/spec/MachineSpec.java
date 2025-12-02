package enigma.shared.spec;

import java.util.Map;
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
     * @return ReflectorSpec instance for the given id, or null if not found
     */
    public ReflectorSpec getReflectorById(String id) {
        return (reflectorsById == null) ? null : reflectorsById.get(id);
    }

    /**
     * Convenience lookup for a rotor specification by its numeric id.
     *
     * @param id rotor id
     * @return RotorSpec instance for the given id, or null if not found
     */
    public RotorSpec getRotorById(int id) {
        return (rotorsById == null) ? null : rotorsById.get(id);
    }

    /**
     * @inheritDoc
     * @return
     */
    @Override
    public String toString() {
        String letters = (alphabet == null) ? null : alphabet.getLetters();
        int alphaSize = (alphabet == null) ? 0 : alphabet.size();

        StringBuilder sb = new StringBuilder();
        sb.append("MachineSpec:\n");
        sb.append("  Alphabet: ").append(letters == null ? "<none>" : letters).append("\n");
        sb.append("  Alphabet size: ").append(alphaSize).append("\n");

        // Print rotors using RotorSpec.toString()
        if (rotorsById == null || rotorsById.isEmpty()) {
            sb.append("  Rotors: <none>\n");
        } else {
            sb.append("  Rotors (count=").append(rotorsById.size()).append("):\n");
            rotorsById.values().stream()
                    .sorted(Comparator.comparing(RotorSpec::id))
                    .forEach(r -> sb.append("    ").append(r).append("\n"));
        }

        // Print reflectors using ReflectorSpec.toString()
        if (reflectorsById == null || reflectorsById.isEmpty()) {
            sb.append("  Reflectors: <none>\n");
        } else {
            sb.append("  Reflectors (count=").append(reflectorsById.size()).append("):\n");
            reflectorsById.values().stream()
                    .sorted(Comparator.comparing(ReflectorSpec::id))
                    .forEach(rf -> sb.append("    ").append(rf).append("\n"));
        }

        return sb.toString();
    }
}
