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
}

