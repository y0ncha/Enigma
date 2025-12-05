package enigma.machine.factory;

import enigma.machine.component.rotor.Rotor;
import enigma.machine.component.rotor.RotorImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory implementation for creating mechanical rotors.
 * 
 * <p>This factory creates rotors using the mechanical model implemented in
 * {@link RotorImpl}. The mechanical model represents rotor wiring as a list
 * of wire pairs, where each wire connects a right contact (entry) to a left
 * contact (exit).</p>
 * 
 * <h2>Factory Responsibilities:</h2>
 * <ol>
 *   <li>Build forward mapping from rotor specifications (right→left wiring)</li>
 *   <li>Build inverse mapping as needed for backward signal flow</li>
 *   <li>Calculate notch index from specification</li>
 *   <li>Construct the mechanical rotor with proper configuration</li>
 *   <li>Apply initial position via {@code setPosition(startPosition)}</li>
 * </ol>
 * 
 * <h2>Mapping Construction:</h2>
 * <p>The rotor specification typically defines the wiring as pairs of
 * alphabet characters. This factory converts those to wire pairs:</p>
 * <ul>
 *   <li>Right values: sequential alphabet indices (0, 1, 2, ..., n-1)</li>
 *   <li>Left values: the mapped output indices according to the wiring spec</li>
 * </ul>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * RotorFactory factory = new RotorFactoryImpl();
 * 
 * // Create a rotor with wiring: A→E, B→K, C→M, ...
 * List<Integer> rightCol = List.of(0, 1, 2, 3, ...);
 * List<Integer> leftCol = List.of(4, 10, 12, ...);
 * int notchIndex = 16; // Q position
 * 
 * Rotor rotor = factory.create(rightCol, leftCol, notchIndex, 0);
 * }</pre>
 */
public class RotorFactoryImpl implements RotorFactory {
    
    /**
     * Creates a mechanical rotor with the specified wiring and initial position.
     * 
     * <p>This method constructs a {@link RotorImpl} using the mechanical model,
     * then sets it to the specified starting position.</p>
     * 
     * @param rightColumn the right column values (entry contacts, typically 0..n-1)
     * @param leftColumn the left column values (exit contacts, defines the wiring)
     * @param notchIndex the notch position that triggers the next rotor
     * @param startPosition the initial window position (alphabet index)
     * @return a fully configured mechanical Rotor
     */
    @Override
    public Rotor create(List<Integer> rightColumn, List<Integer> leftColumn,
                        int notchIndex, int startPosition) {
        Rotor rotor = createMechanicalRotor(rightColumn, leftColumn, notchIndex);
        rotor.setPosition(startPosition);
        return rotor;
    }
    
    /**
     * Builds a forward mapping array from character-based wiring specification.
     * 
     * <p>This helper converts a character wiring string (e.g., "EKMFLGDQVZNTOWYHXUSPAIBRCJ")
     * into an integer array where forwardMapping[i] is the output index when input is i.</p>
     * 
     * @param wiringSpec the wiring specification string (alphabet permutation)
     * @param alphabet the alphabet string defining character-to-index mapping
     * @return array where index i maps to the output index
     * @throws IllegalArgumentException if wiringSpec length doesn't match alphabet length,
     *         or if wiringSpec contains characters not in the alphabet
     */
    public int[] buildForwardMapping(String wiringSpec, String alphabet) {
        validateWiringSpec(wiringSpec, alphabet);
        
        Map<Character, Integer> charToIndex = buildCharToIndexMap(alphabet);
        int[] mapping = new int[alphabet.length()];
        
        for (int i = 0; i < wiringSpec.length(); i++) {
            char outputChar = wiringSpec.charAt(i);
            mapping[i] = charToIndex.get(outputChar);
        }
        
        return mapping;
    }
    
    /**
     * Builds an inverse mapping array from a forward mapping.
     * 
     * <p>The inverse mapping allows efficient backward signal processing:
     * if forwardMapping[a] = b, then inverseMapping[b] = a.</p>
     * 
     * @param forwardMapping the forward wiring mapping
     * @return the inverse mapping array
     */
    public int[] buildInverseMapping(int[] forwardMapping) {
        int[] inverse = new int[forwardMapping.length];
        for (int i = 0; i < forwardMapping.length; i++) {
            inverse[forwardMapping[i]] = i;
        }
        return inverse;
    }
    
    /**
     * Calculates the notch index from a notch character specification.
     * 
     * <p>The notch character indicates which letter position triggers
     * the next rotor to advance.</p>
     * 
     * @param notchChar the notch character (e.g., 'Q' for rotor I)
     * @param alphabet the alphabet string defining character-to-index mapping
     * @return the notch index (0-based)
     */
    public int buildNotchIndex(char notchChar, String alphabet) {
        return alphabet.indexOf(notchChar);
    }
    
    /**
     * Builds right and left columns from a wiring specification.
     * 
     * <p>The right column contains sequential indices (0, 1, 2, ...).
     * The left column contains the corresponding output indices according
     * to the wiring specification.</p>
     * 
     * @param wiringSpec the wiring specification string
     * @param alphabet the alphabet string
     * @return a two-element array: [rightColumn, leftColumn]
     * @throws IllegalArgumentException if wiringSpec length doesn't match alphabet length,
     *         or if wiringSpec contains characters not in the alphabet
     */
    public List<List<Integer>> buildColumns(String wiringSpec, String alphabet) {
        validateWiringSpec(wiringSpec, alphabet);
        
        Map<Character, Integer> charToIndex = buildCharToIndexMap(alphabet);
        List<Integer> rightColumn = new ArrayList<>();
        List<Integer> leftColumn = new ArrayList<>();
        
        for (int i = 0; i < alphabet.length(); i++) {
            rightColumn.add(i);
            char outputChar = wiringSpec.charAt(i);
            leftColumn.add(charToIndex.get(outputChar));
        }
        
        return List.of(rightColumn, leftColumn);
    }
    
    /**
     * Creates the mechanical rotor instance.
     * 
     * <p>This is the core factory method that instantiates the {@link RotorImpl}
     * with the provided column data and notch index.</p>
     * 
     * @param rightColumn the right column (entry contacts)
     * @param leftColumn the left column (exit contacts)
     * @param notchIndex the notch position
     * @return a new RotorImpl instance (not yet positioned)
     */
    private Rotor createMechanicalRotor(List<Integer> rightColumn, 
                                        List<Integer> leftColumn, 
                                        int notchIndex) {
        return new RotorImpl(rightColumn, leftColumn, notchIndex);
    }
    
    /**
     * Builds a character-to-index mapping from an alphabet string.
     * 
     * @param alphabet the alphabet string (e.g., "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
     * @return a map from character to its index position
     */
    private Map<Character, Integer> buildCharToIndexMap(String alphabet) {
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < alphabet.length(); i++) {
            map.put(alphabet.charAt(i), i);
        }
        return map;
    }
    
    /**
     * Validates that a wiring specification is compatible with the alphabet.
     * 
     * @param wiringSpec the wiring specification string
     * @param alphabet the alphabet string
     * @throws IllegalArgumentException if validation fails
     */
    private void validateWiringSpec(String wiringSpec, String alphabet) {
        if (wiringSpec.length() != alphabet.length()) {
            throw new IllegalArgumentException(
                "Wiring spec length (" + wiringSpec.length() + 
                ") must match alphabet length (" + alphabet.length() + ")");
        }
        
        Map<Character, Integer> charToIndex = buildCharToIndexMap(alphabet);
        for (int i = 0; i < wiringSpec.length(); i++) {
            char c = wiringSpec.charAt(i);
            if (!charToIndex.containsKey(c)) {
                throw new IllegalArgumentException(
                    "Wiring spec contains character '" + c + 
                    "' at position " + i + " which is not in the alphabet");
            }
        }
    }
}
