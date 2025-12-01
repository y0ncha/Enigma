package enigma.loader;

import enigma.machine.alphabet.Alphabet;
import enigma.loader.xml.generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import enigma.shared.spec.MachineSpec;
import enigma.shared.spec.ReflectorSpec;
import enigma.shared.spec.RotorSpec;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads and parses Enigma machine specifications from XML files.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Unmarshal XML into generated JAXB objects.</li>
 *   <li>Validate XML content (alphabet, rotors, reflectors) and translate
 *       it into {@link MachineSpec}, {@link RotorSpec} and {@link ReflectorSpec}.</li>
 * </ul>
 *
 * Validation errors and parsing failures are reported as {@link EnigmaLoadingException}.
 */
public class LoaderXml implements Loader {

    private static final List<String> ROMAN_ORDER = List.of("I", "II", "III", "IV", "V");
    private static final int MINIMUM_ROTOR_COUNT = 3;

    /**
     * {@inheritDoc}
     *
     * <p>High level flow: unmarshal XML -> validate/extract alphabet -> rotors -> reflectors
     * and build a {@link MachineSpec}.</p>
     *
     * @param filePath path to XML file
     * @return validated MachineSpec
     * @throws EnigmaLoadingException on parse or validation errors
     */
    @Override
    public MachineSpec loadMachine(String filePath) throws EnigmaLoadingException {
        BTEEnigma root = loadRoot(filePath);

        Alphabet alphabet = extractAlphabet(root);

        Map<Integer, RotorSpec> rotors = extractRotors(root, alphabet);

        Map<String, ReflectorSpec> reflectors = extractReflectors(root, alphabet);

        return new MachineSpec(alphabet, rotors, reflectors);
    }

    /**
     * Read and unmarshal the XML file to the generated JAXB root object.
     *
     * @param filePath path to the XML file
     * @return the JAXB root object representing the XML
     * @throws EnigmaLoadingException when file not found, wrong extension or parsing fails
     */
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

    /**
     * Extract and validate the alphabet string from the JAXB root, returning an
     * {@link Alphabet} instance.
     *
     * @param root JAXB root object
     * @return Alphabet built from cleaned XML ABC content
     * @throws EnigmaLoadingException on validation failure
     */
    private Alphabet extractAlphabet(BTEEnigma root) throws EnigmaLoadingException {
        String rawAbc = root.getABC();
        String cleanAbc = validateAlphabet(rawAbc);
        return new Alphabet(cleanAbc);
    }

    /**
     * Parse rotor definitions and build {@link RotorSpec} entries.
     *
     * @param root JAXB root containing rotors
     * @param alphabet validated Alphabet used for mapping
     * @return map of rotor id -> RotorSpec
     * @throws EnigmaLoadingException on validation/parsing errors
     */
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
            int notchIndex = notch - 1; // XML notch is 1-based; internal spec uses 0-based

            int[] forward = new int[alphabetSize];
            int[] backward = new int[alphabetSize];
            Set<Integer> seenRight = new HashSet<>();
            Set<Integer> seenLeft = new HashSet<>();

            for (BTEPositioning pos : rotorXml.getBTEPositioning()) {

                // Raw string values from XML, e.g. "E", "K", "M"...
                String rightStr = pos.getRight();
                String leftStr  = pos.getLeft();

                // Basic validation: both sides must be a single character
                if (rightStr == null || rightStr.length() != 1 ||
                        leftStr  == null || leftStr.length()  != 1) {
                    throw new EnigmaLoadingException(
                            "Rotor " + rotorXml.getId() +
                                    " has illegal positioning values: right=" + rightStr +
                                    " left=" + leftStr +
                                    " (must be single letters from alphabet)");
                }

                // Convert characters to numeric indices according to alphabet
                char rightChar = rightStr.charAt(0);
                char leftChar  = leftStr.charAt(0);

                int right = alphabet.indexOf(rightChar);
                int left  = alphabet.indexOf(leftChar);

                // indexOf returns -1 when the character is not in the alphabet
                if (right == -1 || left == -1) {
                    throw new EnigmaLoadingException(
                            "Rotor " + rotorXml.getId() +
                                    " uses letters not in alphabet: right=" + rightChar +
                                    " left=" + leftChar);
                }

                // Ensure permutation: each index must appear exactly once on both sides
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

                // Fill forward/backward mapping arrays (right->left and left->right)
                forward[right] = left;
                backward[left] = right;
            }


            if (seenRight.size() != alphabetSize || seenLeft.size() != alphabetSize) {
                throw new EnigmaLoadingException("Rotor " + id +
                        " does not define a full permutation of the alphabet");
            }

            result.put(id, new RotorSpec(id, notchIndex, forward, backward));
        }

        // Validate the rotor id set forms 1..N contiguous sequence
        validateRotorIdSequence(result.keySet());

        return result;
    }

    /**
     * Parse reflector definitions and build {@link ReflectorSpec} entries.
     *
     * @param root JAXB root containing reflectors
     * @param alphabet validated Alphabet used for mapping
     * @return map of reflector id -> ReflectorSpec
     * @throws EnigmaLoadingException on validation/parsing errors
     */
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

    // --- Alphabet validation helper -------------------------------------------------

    /**
     * Validate and clean the raw alphabet string from the XML {@code <ABC>} element.
     *
     * <p>This method expects the raw alphabet value as read from the XML {@code <ABC>} element.
     * It removes all whitespace using {@code rawAbc.replaceAll("\\s+", "")} and then
     * enforces the following constraints on the cleaned alphabet:</p>
     *
     * <ul>
     *   <li>the raw value must not be {@code null} (the XML must contain an {@code <ABC>} element)</li>
     *   <li>the cleaned value must not be empty after whitespace removal</li>
     *   <li>the cleaned value must have an even length</li>
     *   <li>the cleaned value must not contain duplicate characters</li>
     * </ul>
     *
     * <p>On success the cleaned alphabet string (all whitespace removed) is returned.
     * On failure an {@link EnigmaLoadingException} is thrown describing the problem.</p>
     *
     * @param rawAbc raw ABC value from XML
     * @return cleaned alphabet string
     * @throws EnigmaLoadingException when validation fails
     */
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

    // --- Rotor validation helpers --------------------------------------------------

    /**
     * Validate rotor configuration from the <BTE-Rotors> section.
     *
     * @param bteRotors JAXB object representing the <BTE-Rotors> section
     * @throws EnigmaLoadingException if validation fails
     */
    private void validateRotorsHeader(BTERotors bteRotors) throws EnigmaLoadingException {
        if (bteRotors == null || bteRotors.getBTERotor().isEmpty()) {
            throw new EnigmaLoadingException("No <BTE-Rotors> section or empty rotors list");
        }
        if (bteRotors.getBTERotor().size() < MINIMUM_ROTOR_COUNT) {
            throw new EnigmaLoadingException("Machine must define at least " + MINIMUM_ROTOR_COUNT + " rotors, but got " + bteRotors.getBTERotor().size());
        }
    }

    /**
     * Ensure the rotor id is unique within the existing set.
     *
     * @param id rotor id to check
     * @param existing map of existing rotor ids
     * @throws EnigmaLoadingException if the id is not unique
     */
    private void validateRotorIdUnique(int id, Map<Integer, RotorSpec> existing) throws EnigmaLoadingException {
        if (existing.containsKey(id)) {
            throw new EnigmaLoadingException("Duplicate rotor id: " + id);
        }
    }

    /**
     * Validate the notch position of a rotor.
     *
     * @param rotorId id of the rotor
     * @param notch notch position to validate
     * @param alphabetSize size of the alphabet (number of letters)
     * @throws EnigmaLoadingException if the notch position is out of bounds
     */
    private void validateNotch(int rotorId, int notch, int alphabetSize) throws EnigmaLoadingException {
        if (notch < 1 || notch > alphabetSize) {
            throw new EnigmaLoadingException("Rotor " + rotorId +
                    " has illegal notch " + notch + " (must be 1.." + alphabetSize + ")");
        }
    }

    /**
     * Validate that the set of rotor ids forms a contiguous sequence from 1 to N.
     *
     * @param ids set of rotor ids to validate
     * @throws EnigmaLoadingException if the sequence is not contiguous or has gaps
     */
    private void validateRotorIdSequence(Set<Integer> ids) throws EnigmaLoadingException {
        if (ids == null || ids.isEmpty()) return;

        int min = Collections.min(ids);
        int max = Collections.max(ids);
        int expectedCount = max - min + 1;

        if (min != 1 || expectedCount != ids.size()) {
            throw new EnigmaLoadingException("Rotor ids must form a contiguous sequence 1..N without gaps, but got: " + ids);
        }
    }

    // --- Reflector validation helpers ------------------------------------------------

    /**
     * Validate reflector configuration from the <BTE-Reflectors> section.
     *
     * @param bteReflectors JAXB object representing the <BTE-Reflectors> section
     * @throws EnigmaLoadingException if validation fails
     */
    private void validateReflectorsHeader(BTEReflectors bteReflectors) throws EnigmaLoadingException {
        if (bteReflectors == null || bteReflectors.getBTEReflector().isEmpty()) {
            throw new EnigmaLoadingException("No <BTE-Reflectors> section or empty reflectors list");
        }
    }

    /**
     * Validate the format of a reflector id.
     *
     * @param id reflector id to validate
     * @throws EnigmaLoadingException if the id format is invalid
     */
    private void validateReflectorIdFormat(String id) throws EnigmaLoadingException {
        if (!ROMAN_ORDER.contains(id)) {
            throw new EnigmaLoadingException("Illegal reflector id '" + id + "' (must be Roman numeral I, II, III, IV or V)");
        }
    }

    /**
     * Validate that the set of reflector ids forms a contiguous run of Roman numerals starting from I.
     *
     * @param ids set of reflector ids to validate
     * @throws EnigmaLoadingException if the run is not contiguous or does not start from I
     */
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
