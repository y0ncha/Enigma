package enigma.shared.utils;

public class Utils {

    /** Formats the plugStr string into pairs separated by commas.
     * Example: "ABCD" -> "A|B,C|D"
     * @return formatted plugStr string
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
