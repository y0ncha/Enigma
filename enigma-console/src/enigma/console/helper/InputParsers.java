package enigma.console.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Input parsing utilities for console user input.
 *
 * <p><b>Module:</b> enigma-console (parsing helpers)</p>
 *
 * <h2>Purpose</h2>
 * <p>InputParsers provides stateless parsing methods to convert raw user input
 * strings into structured types (lists, characters, etc.) needed for engine
 * configuration.</p>
 *
 * <h2>Parsing Methods</h2>
 * <ul>
 *   <li><b>parseRotorIds:</b> Parse comma-separated rotor IDs (e.g., "1,2,3" → [1, 2, 3])</li>
 *   <li><b>buildInitialPositions:</b> Convert position string to character list (e.g., "ABC" → ['A', 'B', 'C'])</li>
 *   <li><b>toRoman:</b> Convert integer to Roman numeral (e.g., 1 → "I", 2 → "II")</li>
 * </ul>
 *
 * <h2>Validation</h2>
 * <p>Parsing methods perform format validation:</p>
 * <ul>
 *   <li>Reject empty comma parts (e.g., "1,,3" is invalid)</li>
 *   <li>Reject non-numeric values (e.g., "1,abc,3" is invalid)</li>
 *   <li>Throw {@link IllegalArgumentException} with user-friendly messages</li>
 * </ul>
 *
 * <p>Semantic validation (rotor ID existence, position alphabet membership) is
 * performed by the engine, not by these parsers.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Parse rotor IDs from user input
 * String input = scanner.nextLine(); // "1,2,3"
 * List&lt;Integer&gt; rotorIds = InputParsers.parseRotorIds(input);
 * // rotorIds = [1, 2, 3]
 *
 * // Parse positions
 * String positions = scanner.nextLine(); // "ABC"
 * List&lt;Character&gt; posList = InputParsers.buildInitialPositions(positions);
 * // posList = ['A', 'B', 'C']
 *
 * // Convert to Roman numeral for display
 * String roman = InputParsers.toRoman(2); // "II"
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods are static and stateless. Thread-safe.</p>
 *
 * @since 1.0
 */
public class InputParsers {
    // Prevent instantiation
    private InputParsers() {}

    /**
     * Parse comma-separated rotor IDs from user input.
     *
     * <p><b>Format:</b> "1,2,3" → [1, 2, 3]</p>
     *
     * <p><b>Validation:</b></p>
     * <ul>
     *   <li>Each part must be non-empty (rejects "1,,3")</li>
     *   <li>Each part must be a valid integer</li>
     * </ul>
     *
     * <p>The returned list is in left→right order matching user input.</p>
     *
     * @param line raw user input (comma-separated integers)
     * @return list of rotor IDs in left→right order
     * @throws IllegalArgumentException if format is invalid with user-friendly message
     */
    public static List<Integer> parseRotorIds(String line) {
        List<Integer> result = new ArrayList<>();
        String[] parts = line.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException(
                        "Rotor ids must be decimal numbers separated by commas. "
                                + "Empty values are not allowed.");
            }
            try {
                result.add(Integer.parseInt(trimmed));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Rotor ids must be decimal numbers separated by commas. "
                                + "Invalid value: '" + trimmed + "'.");
            }
        }
        return result;
    }
    /**
     * Convert integer to Roman numeral (I-V).
     *
     * <p><b>Mappings:</b></p>
     * <ul>
     *   <li>1 → "I"</li>
     *   <li>2 → "II"</li>
     *   <li>3 → "III"</li>
     *   <li>4 → "IV"</li>
     *   <li>5 → "V"</li>
     *   <li>Other → "?{value}"</li>
     * </ul>
     *
     * @param value integer to convert (typically 1-5)
     * @return Roman numeral string
     */
    public static String toRoman(int value) {
        return switch (value) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "?" + value;
        };
    }

    /**
     * Convert positions string to character list.
     *
     * <p><b>Format:</b> "ABC" → ['A', 'B', 'C']</p>
     *
     * <p>The conversion is case-insensitive (converts to uppercase).
     * The resulting list is in left→right order matching the CodeConfig
     * convention (first character = leftmost rotor).</p>
     *
     * <p><b>Format Only:</b> This method performs string to character list
     * conversion. It does NOT validate alphabet membership (engine responsibility).</p>
     *
     * @param positions position string (e.g., "ABC", "abc", "ODX")
     * @return list of uppercase characters in left→right order
     */
    public static List<Character> buildInitialPositions(String positions) {
        positions = positions.toUpperCase();
        int n = positions.length();
        List<Character> initialPositions = new ArrayList<>(n);
        
        for (int i = 0; i < n; i++) {
            char c = positions.charAt(i);
            // Pass characters in the same order as input (left→right)
            initialPositions.add(c);
        }
        return initialPositions;
    }

}
