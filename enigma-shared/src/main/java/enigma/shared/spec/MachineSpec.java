package enigma.shared.spec;

import java.util.Map;
import java.util.Comparator;
import enigma.shared.alphabet.Alphabet;

/**
 * Represents the specification of an Enigma machine, including its alphabet,
 * available rotors and reflectors.
 *
 * <p><b>Module:</b> enigma-shared (specs)</p>
 *
 * <p>This record serves as an immutable data container holding the validated
 * machine specification loaded from XML. It provides convenient access to
 * rotors and reflectors by their identifiers.</p>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li><b>alphabet:</b> The machine's character set (validated: even-length, unique)</li>
 *   <li><b>rotorsById:</b> Map of rotor ID → RotorSpec (IDs form 1..N sequence)</li>
 *   <li><b>reflectorsById:</b> Map of reflector ID → ReflectorSpec (Roman numerals)</li>
 *   <li><b>rotorsInUse:</b> Number of rotors that must be selected when configuring the machine (e.g. 3)</li>
 * </ul>
 *
 * <h2>Invariants (ensured by loader)</h2>
 * <ul>
 *   <li>Alphabet has even length and unique characters</li>
 *   <li>Rotor IDs form contiguous sequence 1..N</li>
 *   <li>Reflector IDs are Roman numerals starting from "I"</li>
 *   <li>All rotor columns and reflector mappings reference valid alphabet indices</li>
 * </ul>
 *
 * <p><b>Note:</b> Maps are stored as provided (no defensive deep copy) to
 * preserve existing behavior. Callers should not modify returned maps.</p>
 *
 * @param alphabet machine alphabet (even-length, unique chars)
 * @param rotorsById map of rotor ID → RotorSpec
 * @param reflectorsById map of reflector ID → ReflectorSpec
 * @param rotorsInUse number of rotors that must be selected when configuring the machine
 * @since 1.0
 */
public record MachineSpec(
        Alphabet alphabet,
        Map<Integer, RotorSpec> rotorsById,
        Map<String, ReflectorSpec> reflectorsById,
        int rotorsInUse
) {

    /**
     * Convenience lookup for a reflector specification by its identifier.
     *
     * @param id reflector ID (e.g., "I", "II", "III")
     * @return ReflectorSpec instance for the given id, or null if not found
     */
    public ReflectorSpec getReflectorById(String id) {
        return (reflectorsById == null) ? null : reflectorsById.get(id);
    }

    /**
     * Convenience lookup for a rotor specification by its numeric ID.
     *
     * @param id rotor ID (1..N)
     * @return RotorSpec instance for the given id, or null if not found
     */
    public RotorSpec getRotorById(int id) {
        return (rotorsById == null) ? null : rotorsById.get(id);
    }


    /**
     * Generate a multi-line string representation of the machine specification.
     *
     * <p>Includes alphabet, rotor list (sorted by ID), and reflector list (sorted by ID).</p>
     *
     * @return formatted spec summary
     */
    @Override
    public String toString() {
        String letters = (alphabet == null) ? null : alphabet.letters();
        int alphaSize = (alphabet == null) ? 0 : alphabet.size();

        StringBuilder sb = new StringBuilder();
        sb.append("MachineSpec:\n");
        sb.append("  Alphabet: ").append(letters == null ? "<none>" : letters).append("\n");
        sb.append("  Alphabet size: ").append(alphaSize).append("\n");
        sb.append("  Rotors-in-use: ").append(rotorsInUse).append("\n");

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

    /**
     * Returns the total number of reflectors defined in this machine specification.
     *
     * @return the number of reflectors, or 0 if none are defined
     */
    public int getTotalReflectors() {
        return (reflectorsById == null) ? 0 : reflectorsById.size();
    }

    /**
     * Returns the total number of rotors defined in this machine specification.
     *
     * @return the number of rotors, or 0 if none are defined
     */
    public int getTotalRotors() {
        return (rotorsById == null) ? 0 : rotorsById.size();
    }

    /**
     * Returns the number of rotors that must be selected when configuring the machine.
     * This value is part of the MachineSpec and is the authoritative source for
     * rotor count requirements (avoids duplicating a constant across modules).
     *
     * @return required rotors-in-use (typically 3)
     */
    public int getRotorsInUse() {
        return rotorsInUse;
    }

    /**
     * Returns the alphabet string used by this machine.
     *
     * @return the alphabet string (sequence of valid characters)
     */
    public String getAlphabet() {
        return alphabet.letters();
    }

}
