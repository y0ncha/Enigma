package enigma.loader;

import enigma.loader.exception.EnigmaLoadingException;
import enigma.loader.xml.generated.*;
import enigma.shared.alphabet.Alphabet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import enigma.shared.spec.MachineSpec;
import enigma.shared.spec.ReflectorSpec;
import enigma.shared.spec.RotorSpec;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Loads and validates Enigma machine specifications from XML files.
 *
 * <p>Unmarshals XML to JAXB objects, validates alphabet, rotors, and reflectors,
 * and constructs machine specification. Preserves XML wire ordering exactly.</p>
 *
 * @since 1.0
 */
public class LoaderXml implements Loader {

    private static final List<String> ROMAN_ORDER = List.of("I", "II", "III", "IV", "V");
    private int rotorsInUse;

    /**
     * Create loader expecting specified rotor count.
     */
    public LoaderXml() { }

    /**
     * {@inheritDoc}
     */
    @Override
    public MachineSpec loadSpecs(String filePath) throws EnigmaLoadingException {
        BTEEnigma root = loadRoot(filePath);

        rotorsInUse = extractRotorsInUse(root);
        String machineName = validateMachineName(root.getName());

        Alphabet alphabet = extractAlphabet(root);

        Map<Integer, RotorSpec> rotors = extractRotors(root, alphabet);

        Map<String, ReflectorSpec> reflectors = extractReflectors(root, alphabet);

        // Build MachineSpec including rotorsInUse so callers can derive required rotor count
        return new MachineSpec(alphabet, rotors, reflectors, rotorsInUse, machineName);
    }

    /**
     * Unmarshal XML file to JAXB root object with schema validation.
     *
     * @param filePath path to XML file
     * @return JAXB root object
     * @throws EnigmaLoadingException if file not found, wrong extension, or parsing fails
     */
    private BTEEnigma loadRoot(String filePath) throws EnigmaLoadingException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new EnigmaLoadingException("File " + filePath +" not found");
        }

        if (!filePath.toLowerCase().endsWith(".xml")) {
            throw new EnigmaLoadingException("File must have a .xml extension (case-insensitive)");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(BTEEnigma.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Configure schema validation
            Schema schema = loadSchema();
            if (schema != null) {
                unmarshaller.setSchema(schema);
            }

            return (BTEEnigma) unmarshaller.unmarshal(new File(filePath));
        }
        catch (JAXBException e) {
            throw new EnigmaLoadingException("Unable to parse file '" + filePath + "': " + e.getMessage(), e);
        }
    }

    /**
     * Load XSD schema from classpath resources for validation.
     *
     * <p>The schema file is located at: {@code /schema/Enigma-Ex3.xsd} in the classpath
     * (typically {@code src/main/resources/schema/Enigma-Ex3.xsd}).</p>
     *
     * @return Schema instance for validation, or null if schema cannot be loaded
     */
    private Schema loadSchema() {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Load schema from classpath
            InputStream schemaStream = getClass().getResourceAsStream("/schema/Enigma-Ex3.xsd");
            if (schemaStream == null) {
                // Fallback: try loading from URL if available
                // schemaStream = getClass().getResourceAsStream("/schema/Enigma-Ex2.xsd");
                URL schemaUrl = getClass().getResource("/schema/Enigma-Ex3.xsd");
                if (schemaUrl == null) {
                    schemaUrl = getClass().getResource("/schema/Enigma-Ex2.xsd");
                }
                if (schemaUrl != null) {
                    return schemaFactory.newSchema(schemaUrl);
                }
                System.err.println("Warning: XSD schema not found at /schema/Enigma-Ex3.xsd in classpath. Validation skipped.");
                return null;
            }

            return schemaFactory.newSchema(new javax.xml.transform.stream.StreamSource(schemaStream));
        } catch (SAXException e) {
            System.err.println("Warning: Unable to load XSD schema: " + e.getMessage() + ". Validation skipped.");
            return null;
        }
    }

    /**
     * Validate and normalize machine name from XML (Exercise 3).
     *
     * @param rawName name as read from XML
     * @return trimmed non-empty machine name
     */
    private String validateMachineName(String rawName) throws EnigmaLoadingException {
        if (rawName == null) {
            throw new EnigmaLoadingException("Machine name is missing (BTE-Enigma@name)");
        }
        String clean = rawName.trim();
        if (clean.isEmpty()) {
            throw new EnigmaLoadingException("Machine name is empty (BTE-Enigma@name)");
        }
        return clean;
    }

    /**
     * Extract and validate alphabet from JAXB root.
     *
     * @param root JAXB root object
     * @return validated alphabet instance
     * @throws EnigmaLoadingException if alphabet validation fails
     */
    private Alphabet extractAlphabet(BTEEnigma root) throws EnigmaLoadingException {
        String rawAbc = root.getABC();
        String cleanAbc = validateAlphabet(rawAbc);
        return new Alphabet(cleanAbc);
    }

    /**
     * Parse rotor definitions and build {@link RotorSpec} entries.
     *
     * <p><b>Important:</b> Rotor columns are stored in XML row order without
     * reordering. Right/left columns follow the XML &lt;BTE-Positioning&gt;
     * sequence exactly.</p>
     *
     * <p>Validation performed:</p>
     * <ul>
     *   <li>Rotor IDs are unique</li>
     *   <li>Notch is in valid range [1, alphabetSize]</li>
     *   <li>Right and left columns form full permutations (bijectivity)</li>
     *   <li>All characters are in the alphabet</li>
     *   <li>Final ID set forms contiguous sequence 1..N</li>
     * </ul>
     *
     * @param root JAXB root containing rotors
     * @param alphabet validated Alphabet used for mapping
     * @return map of rotor id → RotorSpec
     * @throws EnigmaLoadingException on validation/parsing errors
     */
    private Map<Integer, RotorSpec> extractRotors(BTEEnigma root, Alphabet alphabet) throws EnigmaLoadingException {

        Map<Integer, RotorSpec> result = new java.util.LinkedHashMap<>();

        BTERotors bteRotors = root.getBTERotors();
        validateRotorsHeader(bteRotors);

        int alphabetSize = alphabet.size();

        for (BTERotor rotorXml : bteRotors.getBTERotor()) {
            int id = rotorXml.getId();
            validateRotorIdUnique(id, result);

            int notch = rotorXml.getNotch();
            validateNotch(notch, alphabetSize);
            int notchIndex = notch - 1; // XML notch is 1-based; internal spec uses 0-based

            // Build row-ordered right/left char arrays according to XML ordering
            char[] rightColumn = new char[alphabetSize];
            char[] leftColumn = new char[alphabetSize];
            Set<Integer> seenRight = new HashSet<>();
            Set<Integer> seenLeft = new HashSet<>();

            int rowIdx = 0;
            for (BTEPositioning pos : rotorXml.getBTEPositioning()) {
                String rightStr = pos.getRight();
                String leftStr  = pos.getLeft();

                if (rightStr == null || rightStr.length() != 1 ||
                        leftStr  == null || leftStr.length()  != 1) {
                    throw new EnigmaLoadingException(
                            "Rotor " + rotorXml.getId() +
                                    " has illegal positioning values: right=" + rightStr +
                                    " left=" + leftStr +
                                    " (must be single letters from alphabet)");
                }

                char rightChar = rightStr.charAt(0);
                char leftChar  = leftStr.charAt(0);

                int rightIdx = alphabet.indexOf(rightChar);
                int leftIdx  = alphabet.indexOf(leftChar);

                if (rightIdx == -1 || leftIdx == -1) {
                    throw new EnigmaLoadingException(
                            "Rotor " + rotorXml.getId() +
                                    " uses letters not in alphabet (right=" + rightChar +
                                    " left=" + leftChar + ")");
                }

                if (!seenRight.add(rightIdx)) {
                    throw new EnigmaLoadingException(
                            "Rotor " + id +
                                    " has duplicate mapping for right index " +
                                    (rightIdx + 1) + " (letter: " + rightChar + ")");
                }
                if (!seenLeft.add(leftIdx)) {
                    throw new EnigmaLoadingException(
                            "Rotor " + id +
                                    " has duplicate mapping for left index " +
                                    (leftIdx + 1) + " (letter: " + leftChar + ")");
                }

                if (rowIdx >= alphabetSize) {
                    throw new EnigmaLoadingException("Rotor " + id + " defines more positions than alphabet size");
                }

                rightColumn[rowIdx] = rightChar;
                leftColumn[rowIdx] = leftChar;
                rowIdx++;
            }

            if (seenRight.size() != alphabetSize || seenLeft.size() != alphabetSize) {
                throw new EnigmaLoadingException("Rotor " + id +
                        " does not define a full permutation of the alphabet");
            }

            result.put(id, new RotorSpec(id, notchIndex, rightColumn, leftColumn));
        }

        // Validate the rotor id set forms 1..N contiguous sequence
        validateRotorIdSequence(result.keySet());

        return result;
    }

    /**
     * Parse reflector definitions and build {@link ReflectorSpec} entries.
     *
     * <p><b>Important:</b> Reflector mapping is constructed directly from XML
     * pairs without reordering or sorting. The mapping preserves XML-defined
     * pair relationships exactly.</p>
     *
     * <p>Validation performed:</p>
     * <ul>
     *   <li>Reflector IDs are unique</li>
     *   <li>IDs follow Roman numeral format (I, II, III, ...)</li>
     *   <li>Mapping is symmetric: if i→j then j→i</li>
     *   <li>No self-mapping (i→i is forbidden)</li>
     *   <li>All indices [0, alphabetSize) are covered</li>
     *   <li>Final ID set starts from "I" and forms contiguous Roman sequence</li>
     * </ul>
     *
     * @param root JAXB root containing reflectors
     * @param alphabet validated Alphabet used for mapping
     * @return map of reflector id → ReflectorSpec
     * @throws EnigmaLoadingException on validation/parsing errors
     */
    private Map<String, ReflectorSpec> extractReflectors(BTEEnigma root, Alphabet alphabet) throws EnigmaLoadingException {

        Map<String, ReflectorSpec> result = new java.util.LinkedHashMap<>();

        BTEReflectors bteReflectors = root.getBTEReflectors();
        validateReflectorsHeader(bteReflectors);

        int alphabetSize = alphabet.size();

        for (BTEReflector refXml : bteReflectors.getBTEReflector()) {
            String id = refXml.getId();
            if (result.containsKey(id)) {
                throw new EnigmaLoadingException("Duplicate reflector id (" + id + ")");
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
                            " mapping out of range (" + (in + 1) + " <-> " + (out + 1) +")");
                }

                if (in == out) {
                    throw new EnigmaLoadingException("Reflector " + id +
                            " maps letter to itself at position " + (in + 1));
                }

                if (mapping[in] != -1) {
                    throw new EnigmaLoadingException("Reflector '" + id +
                            "' reuses index " + (in + 1));
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

        // After building reflectors, ensure ids form a contiguous Roman main starting from I
        validateReflectorIdRun(result.keySet());
        return result;
    }

    // --- Alphabet validation helper -------------------------------------------------

    /**
     * Validate and clean the raw alphabet string from the XML {@code <ABC>} element.
     *
     * <p>This method trims only leading/trailing whitespace from the raw value and
     * preserves inner spaces as real alphabet characters.</p>
     *
     * <ul>
     *   <li>the raw value must not be {@code null} (the XML must contain an {@code <ABC>} element)</li>
     *   <li>the cleaned value must not be empty after edge trimming</li>
     *   <li>the cleaned value must have an even length</li>
     *   <li>the cleaned value must not contain duplicate characters</li>
     * </ul>
     *
     * @param rawAbc raw ABC value from XML
     * @return cleaned alphabet string
     * @throws EnigmaLoadingException when validation fails
     */
    private String validateAlphabet(String rawAbc) throws EnigmaLoadingException {
        if (rawAbc == null) {
            throw new EnigmaLoadingException("<ABC> section is missing");
        }

        String cleanAbc = rawAbc.trim();
        if (cleanAbc.isEmpty()) {
            throw new EnigmaLoadingException("<ABC> section is empty after trimming");
        }

        if (cleanAbc.length() % 2 != 0) {
            throw new EnigmaLoadingException(
                String.format("Alphabet must have even length, but got %d characters (Alphabet: %s)", cleanAbc.length(), cleanAbc));
        }

        // Check for duplicate characters
        Set<Character> charSet = new HashSet<>();
        for (char c : cleanAbc.toCharArray()) {
            if (!charSet.add(c)) {
                throw new EnigmaLoadingException(
                    String.format("Character '%c' appears more than once", c));
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
            throw new EnigmaLoadingException(
                    "<BTE-Rotors> section is missing");
        }
        if (bteRotors.getBTERotor().isEmpty()) {
            throw new EnigmaLoadingException(
                    "<BTE-Rotors> section is empty");
        }
        if (bteRotors.getBTERotor().size() < rotorsInUse) {
            throw new EnigmaLoadingException(
                    "<BTE-Rotors> section must include at least " + rotorsInUse + " rotor specifications, got "  + bteRotors.getBTERotor().size());
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
            throw new EnigmaLoadingException(
                String.format("Rotor ID %d appears more than once", id));
        }
    }

    /**
     * Validate the notch position of a rotor.
     *
     * @param notch notch position to validate
     * @param alphabetSize size of the alphabet (number of letters)
     * @throws EnigmaLoadingException if the notch position is out of bounds
     */
    private void validateNotch(int notch, int alphabetSize) throws EnigmaLoadingException {
        if (notch < 1 || notch > alphabetSize) {
            throw new EnigmaLoadingException("Notch position is out of bounds");
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
            throw new EnigmaLoadingException("Rotor IDs must form a contiguous sequence starting from 1 (e.g., 1, 2, 3, 4). Got " + ids);
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
        if (bteReflectors == null) {
            throw new EnigmaLoadingException("<BTE-Reflectors> section is missing");
        }
        if (bteReflectors.getBTEReflector().isEmpty()) {
            throw new EnigmaLoadingException("<BTE-Reflectors> section is empty");
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
            throw new EnigmaLoadingException(
                String.format("Reflector ID '%s' is not a valid Roman numeral " + "(valid reflector IDs: I, II, III, IV, V)", id));
        }
    }

    /**
     * Validate that the set of reflector ids forms a contiguous sequence of Roman numerals starting from I.
     *
     * @param ids set of reflector ids to validate
     * @throws EnigmaLoadingException if the sequence is not contiguous or does not start from I
     */
    private void validateReflectorIdRun(Set<String> ids) throws EnigmaLoadingException {
        if (ids == null || ids.isEmpty()) return;

        // Build the required prefix of ROMAN_ORDER with size n
        int n = ids.size();
        for (int i = 0; i < n; i++) {
            String required = ROMAN_ORDER.get(i);
            if (!ids.contains(required)) {
                throw new EnigmaLoadingException(
                        "Reflector IDs must form a contiguous Roman sequence starting from I (e.g., I, II, III), " + "Got " + ids + " Missing  " + required);
            }
        }
    }
    /**
     * Extract the rotors-in-use value from the JAXB root and validate it.
     *
     * <p>Validation rules:
     * <ul>
     *   <li>The {@code rotors-count} attribute must exist</li>
     *   <li>Value must be a positive integer (>= 1)</li>
     *   <li>If rotors are defined, {@code rotors-count} cannot be greater
     *       than the number of available rotors in {@code <BTE-Rotors>}</li>
     * </ul>
     *
     * @param root JAXB root object
     * @return validated rotors-in-use value
     * @throws EnigmaLoadingException if validation fails
     */
    private int extractRotorsInUse(BTEEnigma root) throws EnigmaLoadingException {
        BigInteger raw = root.getRotorsCount();
        if (raw == null) {
            throw new EnigmaLoadingException(
                    "Missing required 'rotors-count' attribute on <BTE-Enigma> element");
        }

        int value;
        try {
            value = raw.intValueExact();
        } catch (ArithmeticException ex) {
            throw new EnigmaLoadingException(
                    "'rotors-count' value " + raw + " is out of valid int range", ex);
        }

        if (value <= 0) {
            throw new EnigmaLoadingException(
                    "'rotors-count' must be a positive integer, got " + value);
        }

        // Optional consistency check against defined rotors
        BTERotors bteRotors = root.getBTERotors();
        if (bteRotors != null && !bteRotors.getBTERotor().isEmpty()) {
            int definedRotors = bteRotors.getBTERotor().size();
            if (value > definedRotors) {
                throw new EnigmaLoadingException(
                        "'rotors-count' (" + value + ") cannot be greater than the number of " +
                                "defined rotors (" + definedRotors + ")");
            }
        }

        return value;
    }

}
