package enigma.console.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InputParsers {

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
     * Converts user input positions string into a list of 0-based numeric indices,
     * in left→right order, according to CodeConfig requirements.
     * The first character in the input belongs to the RIGHTMOST rotor.
     * Case-insensitive: both uppercase and lowercase letters are accepted.
     */
    public static List<Integer> buildInitialPositions(String positions) {
        // Normalize input to uppercase to allow lowercase letters
        positions = positions.toUpperCase();
        int n = positions.length();
        List<Integer> initialPositions = new ArrayList<>(Collections.nCopies(n, 0));
        for (int i = 0; i < n; i++) {
            char c = positions.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException(
                        "Initial position '" + c + "' is not a valid letter (A-Z, case-insensitive).");
            }
            int index = c - 'A';
            // The first character corresponds to the RIGHTMOST rotor
            int leftIndex = n - 1 - i;
            initialPositions.set(leftIndex, index);
        }
        return initialPositions;
    }

}
