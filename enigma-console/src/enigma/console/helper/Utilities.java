package enigma.console.helper;

/**
 * Utility class for console output and basic input parsing.
 * Contains only static methods and cannot be instantiated.
 */
public final class Utilities {
    private static final java.util.Scanner scanner = new java.util.Scanner(System.in);
    private Utilities() {
        // Prevent instantiation
    }

    /**
     * Prints a generic error message in a consistent formatted style.
     */
    public static void printError(String message) {
        System.out.println("[ERROR] " + message);
    }

    /**
     * Prints a generic info message in a consistent format.
     */
    public static void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    /**
     * Reads an integer from the user with validation.
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                printError("No input provided. Please enter a number.");
                continue;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                printError("Invalid input. Please enter a valid integer number.");
            }
        }
    }

    /**
     * Reads a non-empty line from the user and returns it trimmed.
     */
    public static String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.println(prompt);
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line != null) {
                line = line.trim();
            }
            if (line == null || line.isEmpty()) {
                Utilities.printError("Input cannot be empty. Please try again.");
                continue;
            }
            return line;
        }
    }
    /**
     * Asks the user a yes/no question and returns true for YES, false for NO.
     */
    public static boolean askUserToRetry(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().toUpperCase();
            if (line.equals("Y") || line.equals("YES")) {
                return true;
            }
            if (line.equals("N") || line.equals("NO")) {
                return false;
            }
            printError("Please answer with 'Y' or 'N'.");
        }
    }

}
