package enigma.console.helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Input parsing utilities for console user input.
 *
 * <p>Converts raw user input strings into structured types with format validation.</p>
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
     * Parse a raw positions string into a list of uppercase characters.
     *
     * <p><b>Expected format:</b> a continuous sequence of letters with no spaces
     * or separators. Examples: "CCC", "abc", "AzQ".</p>
     *
     * <p><b>Validation:</b></p>
     * <ul>
     *   <li>Input must be non-null and non-empty</li>
     * </ul>
     *
     * <p>The returned list preserves user order and converts all characters to uppercase.</p>
     *
     * @param line raw user input containing rotor positions (e.g., "abc", "CCC")
     * @return list of uppercase characters in left→right order
     * @throws IllegalArgumentException if the input format is invalid
     */
    public static List<Character> parsePositions(String line) {
        // Basic null/empty validation
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException(
                    "Positions must contain at least one character, Empty input is not allowed");
        }
        // Trim input and convert to uppercase for consistency
        String cleaned = line.trim().toUpperCase();
        List<Character> result = new ArrayList<>();
        // Validate each character and collect
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException(
                        String.format(
                                "Invalid position character: '%c'. All Positions must be alphabet letters",
                                c));
            }
            result.add(c);
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
