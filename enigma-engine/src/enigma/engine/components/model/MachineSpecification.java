package enigma.engine.components.model;
import java.util.Map;
import enigma.machine.component.alphabet.Alphabet;

/**
 * Represents the specification of an Enigma machine, including its alphabet,
 * available rotors and reflectors, and the number of rotors used in operation.
 * This class serves as a model for configuring and describing the machine's
 * components and constraints.
 *
 * @since 1.0
 */
public class MachineSpecification {
    private final Alphabet alphabet;
    private final Map<Integer, RotorSpecification> rotorsById;
    private final Map<String, ReflectorSpecification> reflectorsById;
    private final int rotorsCountInUse; // Used in exercise 2+

    public MachineSpecification(Alphabet alphabet,
                                Map<Integer, RotorSpecification> rotorsById,
                                Map<String, ReflectorSpecification> reflectorsById,
                                int rotorsCountInUse) {
        this.alphabet = alphabet;
        this.rotorsById = rotorsById;
        this.reflectorsById = reflectorsById;
        this.rotorsCountInUse = rotorsCountInUse;
    }

    public Alphabet getAlphabet() {
        return alphabet;
    }

    public Map<Integer, RotorSpecification> getRotorsById() {
        return rotorsById;
    }

    public Map<String, ReflectorSpecification> getReflectorsById() {
        return reflectorsById;
    }

    public int getRotorsCountInUse() {
        return rotorsCountInUse;
    }
}
