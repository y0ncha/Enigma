package enigma.shared.utils;

/**
 * Utility methods for shared operations across modules.
 *
 * <p><b>Module:</b> enigma-shared (utilities)</p>
 *
 * <p>Provides stateless helper methods for common formatting and conversion
 * operations used throughout the Enigma application.</p>
 *
 * @since 1.0
 */
public class Utils {

    /**
     * Prevent instantiation of utility class.
     */
    private Utils() {}

    /**
     * Format plugboard string into pipe-separated pairs.
     *
     * <p>Converts a plugboard configuration string into a human-readable format
     * with each character pair separated by a pipe and pairs separated by commas.</p>
     *
     * <p><b>Example:</b> "ABCD" → "A|B,C|D"</p>
     *
     * @param plugStr plugboard configuration string (even length)
     * @return formatted plugboard string with pipe-separated pairs
     * @since 1.0
     */
    public static String formatPlugboard(String plugStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plugStr.length(); i += 2) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(plugStr.charAt(i))
                    .append("|")
                    .append(plugStr.charAt(i + 1));
        }
        return sb.toString();
    }
}
