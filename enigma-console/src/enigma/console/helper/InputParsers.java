package enigma.console.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing and converting user input related to Enigma machine configuration.
 * <p>
 * Provides static methods to:
 * <ul>
 *   <li>Parse comma-separated rotor IDs from a string ({@link #parseRotorIds(String)})</li>
 *   <li>Convert integer values to Roman numerals ({@link #toRoman(int)})</li>
 *   <li>Build initial rotor positions from a string input ({@link #buildInitialPositions(String)})</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 *   List&lt;Integer&gt; rotorIds = InputParsers.parseRotorIds("1,2,3");
 *   String roman = InputParsers.toRoman(2); // "II"
 *   List&lt;Integer&gt; positions = InputParsers.buildInitialPositions("ABC");
 * </pre>
 * <p>
 * Note: All methods are static and this class should not be instantiated.
 */
public class InputParsers {
    // Prevent instantiation
    private InputParsers() {}

    public static List<Integer> parseRotorIds(String line) {
        List<Integer> result = new ArrayList<>();
        String[] parts = line.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
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
     * Converts user input positions string into a list of characters
     * in left→right order, according to CodeConfig requirements.
     * The first character in the input corresponds to the LEFTMOST rotor,
     * matching the left→right convention used throughout the architecture.
     * Case-insensitive: both uppercase and lowercase letters are accepted.
     */
    public static List<Character> buildInitialPositions(String positions) {
        positions = positions.toUpperCase();
        int n = positions.length();
        List<Character> initialPositions = new ArrayList<>(n);
        
        for (int i = 0; i < n; i++) {
            char c = positions.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException(
                        "Initial position '" + c + "' is not a valid letter (A-Z, case-insensitive).");
            }
            // Pass characters in the same order as input (left→right)
            initialPositions.add(c);
        }
        return initialPositions;
    }

}
