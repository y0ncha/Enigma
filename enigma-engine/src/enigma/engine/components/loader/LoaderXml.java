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
 * <p>
 * Implementation details:
 * <ul>
 *   <li>Uses {@link BTEEnigma} as the root JAXB-mapped class.</li>
 *   <li>Validates the alphabet section for even length and non-empty content.</li>
 *   <li>Builds collections of rotors and reflectors from the XML structure.</li>
 * </ul>
 */
public class LoaderXml implements Loader {

    @Override
    public MachineSpec loadMachine(String filePath) throws EnigmaLoadingException {
        BTEEnigma root = loadRootFromFile(filePath);

        Alphabet alphabet = buildAlphabet(root);

        Map<Integer, RotorSpec> rotors = buildRotors(root, alphabet);

        Map<String, ReflectorSpec> reflectors = buildReflectors(root, alphabet);

        int rotorsCountInUse = 0;

        return new MachineSpec(alphabet, rotors, reflectors, rotorsCountInUse);
    }


    private BTEEnigma loadRootFromFile(String filePath) throws EnigmaLoadingException {
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


    private Alphabet buildAlphabet(BTEEnigma root) throws EnigmaLoadingException {
        String rawAbc = root.getABC();
        if (rawAbc == null) {
            throw new EnigmaLoadingException("XML does not contain <ABC> section");
        }

        String cleanAbc = rawAbc.replaceAll("\\s+", "");
        if (cleanAbc.isEmpty()) {
            throw new EnigmaLoadingException("<ABC> section is empty after trimming");
        }

        if (cleanAbc.length() % 2 != 0) {
            throw new EnigmaLoadingException(
                    "Alphabet length must be even, but got " + cleanAbc.length());
        }

        // Check for duplicate characters
        Set<Character> charSet = new HashSet<>();
        for (char c : cleanAbc.toCharArray()) {
            if (!charSet.add(c)) {
                throw new EnigmaLoadingException("Alphabet contains duplicate character: '" + c + "'");
            }
        }
        return new Alphabet(cleanAbc);
    }


    private Map<Integer, RotorSpec> buildRotors(BTEEnigma root,
                                                Alphabet alphabet)
        throws EnigmaLoadingException {

        Map<Integer, RotorSpec> result = new HashMap<>();

        BTERotors bteRotors = root.getBTERotors();
        if (bteRotors == null || bteRotors.getBTERotor().isEmpty()) {
            throw new EnigmaLoadingException("No <BTE-Rotors> section or empty rotors list");
        }

        int alphabetSize = alphabet.size();

        for (BTERotor rotorXml : bteRotors.getBTERotor()) {
            int id = rotorXml.getId();
            if (result.containsKey(id)) {
                throw new EnigmaLoadingException("Duplicate rotor id: " + id);
            }

            int notch = rotorXml.getNotch();
            if (notch < 1 || notch > alphabetSize) {
                throw new EnigmaLoadingException("Rotor " + id +
                        " has illegal notch " + notch + " (must be 1.." + alphabetSize + ")");
            }
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

        return result;
    }


    private Map<String, ReflectorSpec> buildReflectors(BTEEnigma root,
                                                       Alphabet alphabet)
            throws EnigmaLoadingException {

        Map<String, ReflectorSpec> result = new HashMap<>();

        BTEReflectors bteReflectors = root.getBTEReflectors();
        if (bteReflectors == null || bteReflectors.getBTEReflector().isEmpty()) {
            throw new EnigmaLoadingException("No <BTE-Reflectors> section or empty reflectors list");
        }

        int alphabetSize = alphabet.size();

        for (BTEReflector refXml : bteReflectors.getBTEReflector()) {
            String id = refXml.getId();
            if (result.containsKey(id)) {
                throw new EnigmaLoadingException("Duplicate reflector id: " + id);
            }

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

        return result;
    }
}
