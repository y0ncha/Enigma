package enigma.engine.components.loader;

import enigma.machine.component.alphabet.Alphabet;
import enigma.engine.xml.generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import enigma.engine.components.model.MachineSpec;
import enigma.engine.components.model.ReflectorSpec;
import enigma.engine.components.model.RotorSpec;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads and parses Enigma machine specifications from XML files.
 * <p>
 * This class implements the {@link Loader} interface and uses JAXB to unmarshal XML files
 * into Java objects representing the Enigma machine configuration. It performs validation
 * on the input XML, including checks for file existence, file extension, and the structure
 * and content of the XML sections (such as <ABC>, rotors, and reflectors).
 * <p>
 * Any errors encountered during loading or parsing are reported via {@link EnigmaLoadingException}.
 */
public class LoaderXml implements Loader {

    private static final List<String> ROMAN_ORDER = List.of("I", "II", "III", "IV", "V");

    @Override
    public MachineSpec loadMachine(String filePath) throws EnigmaLoadingException {
        BTEEnigma root = loadRoot(filePath);

        Alphabet alphabet = extractAlphabet(root);

        Map<Integer, RotorSpec> rotors = extractRotors(root, alphabet);

        Map<String, ReflectorSpec> reflectors = extractReflectors(root, alphabet);

        return new MachineSpec(alphabet, rotors, reflectors);
    }

    private BTEEnigma loadRoot(String filePath) throws EnigmaLoadingException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new EnigmaLoadingException("File does not exist: " + filePath);
        }

        if (!filePath.toLowerCase().endsWith(".xml")) {
            throw new EnigmaLoadingException("File is not an XML (must have a .xml extension, case-insensitive)");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(BTEEnigma.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (BTEEnigma) unmarshaller.unmarshal(new File(filePath));
        } catch (JAXBException e) {
            throw new EnigmaLoadingException("Failed to parse XML with JAXB", e);
        }
    }

    private Alphabet extractAlphabet(BTEEnigma root) throws EnigmaLoadingException {
        String rawAbc = root.getABC();
        String cleanAbc = validateAlphabet(rawAbc);
        return new Alphabet(cleanAbc);
    }

    private Map<Integer, RotorSpec> extractRotors(BTEEnigma root, Alphabet alphabet) throws EnigmaLoadingException {

        Map<Integer, RotorSpec> result = new HashMap<>();

        BTERotors bteRotors = root.getBTERotors();
        validateRotorsHeader(bteRotors);

        int alphabetSize = alphabet.size();

        for (BTERotor rotorXml : bteRotors.getBTERotor()) {
            int id = rotorXml.getId();
            validateRotorIdUnique(id, result);

            int notch = rotorXml.getNotch();
            validateNotch(id, notch, alphabetSize);
            int notchIndex = notch - 1;

            int[] forward = new int[alphabetSize];
            int[] backward = new int[alphabetSize];
            Set<Integer> seenRight = new HashSet<>();
            Set<Integer> seenLeft = new HashSet<>();

            for (BTEPositioning pos : rotorXml.getBTEPositioning()) {

                // Raw string values from XML, e.g. "E", "K", "M"...
                String rightStr = pos.getRight();
                String leftStr  = pos.getLeft();

                // 1. Basic validation: both sides must be a single character
                if (rightStr == null || rightStr.length() != 1 ||
                        leftStr  == null || leftStr.length()  != 1) {
                    throw new EnigmaLoadingException(
                            "Rotor " + rotorXml.getId() +
                                    " has illegal positioning values: right=" + rightStr +
                                    " left=" + leftStr +
                                    " (must be single letters from alphabet)");
                }

                // 2. Convert to chars
                char rightChar = rightStr.charAt(0);
                char leftChar  = leftStr.charAt(0);

                // 3. Map letters to indices according to the current alphabet
                //    e.g. 'A' -> 0, 'B' -> 1, ..., 'Z' -> 25
                int right = alphabet.indexOf(rightChar);
                int left  = alphabet.indexOf(leftChar);

                // If indexOf returns -1, the letter is not in the alphabet
                if (right == -1 || left == -1) {
                    throw new EnigmaLoadingException(
                            "Rotor " + rotorXml.getId() +
                                    " uses letters not in alphabet: right=" + rightChar +
                                    " left=" + leftChar);
                }

                // 5. Ensure permutation: each index on the right and left
                //    appears exactly once (no duplicates)
                if (!seenRight.add(right)) {
                    throw new EnigmaLoadingException(
                            "Rotor " + id +
                                    " has duplicate mapping for right index " +
                                    (right + 1) + " (letter: " + rightChar + ")");
                }
                if (!seenLeft.add(left)) {
                    throw new EnigmaLoadingException(
                            "Rotor " + id +
                                    " has duplicate mapping for left index " +
                                    (left + 1) + " (letter: " + leftChar + ")");
                }

                // 6. Fill forward and backward mapping arrays
                //    forward: index on right side -> index on left side
                //    backward: index on left side -> index on right side
                forward[right] = left;
                backward[left] = right;
            }


            if (seenRight.size() != alphabetSize || seenLeft.size() != alphabetSize) {
                throw new EnigmaLoadingException("Rotor " + id +
                        " does not define a full permutation of the alphabet");
            }

            result.put(id, new RotorSpec(id, notchIndex, forward, backward));
        }

        // After collecting all rotor ids, ensure they form a contiguous 1..N sequence
        validateRotorIdSequence(result.keySet());

        return result;
    }

    // Alphabet validation helper
    private String validateAlphabet(String rawAbc) throws EnigmaLoadingException {
        if (rawAbc == null) {
            throw new EnigmaLoadingException("XML does not contain <ABC> section");
        }

        String cleanAbc = rawAbc.replaceAll("\\s+", "");
        if (cleanAbc.isEmpty()) {
            throw new EnigmaLoadingException("<ABC> section is empty after trimming");
        }

        if (cleanAbc.length() % 2 != 0) {
            throw new EnigmaLoadingException("Alphabet length must be even, but got " + cleanAbc.length());
        }

        // Check for duplicate characters
        Set<Character> charSet = new HashSet<>();
        for (char c : cleanAbc.toCharArray()) {
            if (!charSet.add(c)) {
                throw new EnigmaLoadingException("Alphabet contains duplicate character: '" + c + "'");
            }
        }
        return cleanAbc;
    }

    // Rotor validation helpers
    private void validateRotorsHeader(BTERotors bteRotors) throws EnigmaLoadingException {
        if (bteRotors == null || bteRotors.getBTERotor().isEmpty()) {
            throw new EnigmaLoadingException("No <BTE-Rotors> section or empty rotors list");
        }
        if (bteRotors.getBTERotor().size() < 3) {
            throw new EnigmaLoadingException("Machine must define at least 3 rotors, but got " + bteRotors.getBTERotor().size());
        }
    }

    private void validateRotorIdUnique(int id, Map<Integer, RotorSpec> existing) throws EnigmaLoadingException {
        if (existing.containsKey(id)) {
            throw new EnigmaLoadingException("Duplicate rotor id: " + id);
        }
    }

    private void validateNotch(int rotorId, int notch, int alphabetSize) throws EnigmaLoadingException {
        if (notch < 1 || notch > alphabetSize) {
            throw new EnigmaLoadingException("Rotor " + rotorId +
                    " has illegal notch " + notch + " (must be 1.." + alphabetSize + ")");
        }
    }

    private void validateRotorIdSequence(Set<Integer> ids) throws EnigmaLoadingException {
        if (ids == null || ids.isEmpty()) return;

        int min = Collections.min(ids);
        int max = Collections.max(ids);
        int expectedCount = max - min + 1;

        if (min != 1 || expectedCount != ids.size()) {
            throw new EnigmaLoadingException("Rotor ids must form a contiguous sequence 1..N without gaps, but got: " + ids);
        }
    }

    private Map<String, ReflectorSpec> extractReflectors(BTEEnigma root, Alphabet alphabet) throws EnigmaLoadingException {

        Map<String, ReflectorSpec> result = new HashMap<>();

        BTEReflectors bteReflectors = root.getBTEReflectors();
        validateReflectorsHeader(bteReflectors);

        int alphabetSize = alphabet.size();

        for (BTEReflector refXml : bteReflectors.getBTEReflector()) {
            String id = refXml.getId();
            if (result.containsKey(id)) {
                throw new EnigmaLoadingException("Duplicate reflector id: " + id);
            }

            validateReflectorIdFormat(id);

            int[] mapping = new int[alphabetSize];
            Arrays.fill(mapping, -1);

            for (BTEReflect pair : refXml.getBTEReflect()) {
                int in  = pair.getInput()  - 1;
                int out = pair.getOutput() - 1;

                if (in < 0 || in >= alphabetSize ||
                        out < 0 || out >= alphabetSize) {
                    throw new EnigmaLoadingException("Reflector " + id +
                            " mapping out of range: " + (in + 1) + "<->" + (out + 1));
                }

                if (in == out) {
                    throw new EnigmaLoadingException("Reflector " + id +
                            " maps letter to itself at position " + (in + 1));
                }

                if (mapping[in] != -1) {
                    throw new EnigmaLoadingException("Reflector " + id +
                            " reuses index " + (in + 1));
                }
                if (mapping[out] != -1) {
                    throw new EnigmaLoadingException("Reflector " + id +
                            " reuses index " + (out + 1));
                }

                mapping[in] = out;
                mapping[out] = in;
            }

            for (int i = 0; i < alphabetSize; i++) {
                if (mapping[i] == -1) {
                    throw new EnigmaLoadingException("Reflector " + id +
                            " does not cover index " + (i + 1));
                }
            }

            result.put(id, new ReflectorSpec(id, mapping));
        }

        // After building reflectors, ensure ids form a contiguous Roman run starting from I
        validateReflectorIdRun(result.keySet());

        return result;
    }

    // Reflector validation helpers
    private void validateReflectorsHeader(BTEReflectors bteReflectors) throws EnigmaLoadingException {
        if (bteReflectors == null || bteReflectors.getBTEReflector().isEmpty()) {
            throw new EnigmaLoadingException("No <BTE-Reflectors> section or empty reflectors list");
        }
    }

    private void validateReflectorIdFormat(String id) throws EnigmaLoadingException {
        if (!ROMAN_ORDER.contains(id)) {
            throw new EnigmaLoadingException("Illegal reflector id '" + id + "' (must be Roman numeral I, II, III, IV or V)");
        }
    }

    private void validateReflectorIdRun(Set<String> ids) throws EnigmaLoadingException {
        if (ids == null || ids.isEmpty()) return;

        // Build the required prefix of ROMAN_ORDER with size n
        int n = ids.size();
        for (int i = 0; i < n; i++) {
            String required = ROMAN_ORDER.get(i);
            if (!ids.contains(required)) {
                throw new EnigmaLoadingException("Reflector ids must form a contiguous Roman run starting from I (e.g. I,II,III). Got: " + ids);
            }
        }
    }
}
