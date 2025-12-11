package enigma.console.helper;

/**
 * Console output and input utilities.
 *
 * <p><b>Module:</b> enigma-console (utility helpers)</p>
 *
 * <h2>Purpose</h2>
 * <p>Utilities provides stateless helper methods for console I/O operations,
 * including formatted printing and validated input reading.</p>
 *
 * <h2>Output Methods</h2>
 * <ul>
 *   <li><b>printError:</b> Print error message (no automatic prefix)</li>
 *   <li><b>printInfo:</b> Print info message (no automatic prefix)</li>
 *   <li><b>printFix:</b> Print fix suggestion message (no automatic prefix)</li>
 * </ul>
 *
 * <h2>Input Methods</h2>
 * <ul>
 *   <li><b>readInt:</b> Read and validate integer with retry loop</li>
 *   <li><b>readNonEmptyLine:</b> Read non-empty line with retry loop</li>
 *   <li><b>askUserToRetry:</b> Ask yes/no question with validation</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Print messages
 * Utilities.printInfo("Welcome to Enigma!");
 * Utilities.printError("Invalid input");
 *
 * // Read validated input
 * int choice = Utilities.readInt(scanner, "Enter choice: ");
 * String path = Utilities.readNonEmptyLine(scanner, "Enter path: ");
 * boolean retry = Utilities.askUserToRetry(scanner, "Retry? (Y/N): ");
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p>All methods are static. Methods that read from Scanner are not thread-safe
 * (Scanner itself is not thread-safe).</p>
 *
 * @since 1.0
 */
public final class Utilities {
    private Utilities() {
        // Prevent instantiation
    }

    /**
     * Read and validate an integer from user with retry loop.
     *
     * <p>Continues prompting until valid integer is entered.
     * Handles empty input and non-numeric input with error messages.</p>
     *
     * @param scanner input scanner
     * @param prompt prompt message to display
     * @return validated integer
     */
    public static int readInt(java.util.Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.print("No input provided. Please enter a number.");
                continue;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid integer number.");
            }
        }
    }

    /**
     * Read a non-empty line from user with retry loop.
     *
     * <p>Continues prompting until non-empty line is entered.
     * Trims whitespace from input.</p>
     *
     * @param scanner input scanner
     * @param prompt prompt message to display
     * @return non-empty trimmed line
     */
    public static String readNonEmptyLine(java.util.Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.print("Input cannot be empty. Please try again");
                continue;
            }
            return line;
        }
    }
    /**
     * Ask user a yes/no question with validation.
     *
     * <p>Accepts: Y, YES, N, NO (case-insensitive)</p>
     * <p>Continues prompting until valid answer is entered.</p>
     *
     * @param scanner input scanner
     * @param prompt prompt message to display
     * @return true for Y/YES, false for N/NO
     */
    public static boolean askUserToRetry(java.util.Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().toUpperCase();
            if (line.equals("Y") || line.equals("YES")) {
                return true;
            }
            if (line.equals("N") || line.equals("NO")) {
                return false;
            }
            System.out.println("Please answer with 'Y' or 'N'");
        }
    }

}
