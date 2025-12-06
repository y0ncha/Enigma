package enigma.console;

/**
 * Console interface for the Enigma machine user interface.
 *
 * Implementations handle all user interaction via Scanner and System.out.
 * The console exposes a single entry point: run() - which shows the menu
 * and dispatches user commands in a loop until exit.
 *
 * All numbering for user interaction starts at 1.
 *
 * @since 1.0
 */
public interface Console {

    /**
     * Starts the main console loop:
     * - show menu
     * - read user command
     * - execute command
     * - repeat until Exit is chosen
     */
    void run();
}
