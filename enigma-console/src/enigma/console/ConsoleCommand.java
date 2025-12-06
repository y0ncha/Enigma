package enigma.console;

/**
 * All supported console commands for Exercise 1.
 * The numeric id is what the user types in the menu (1..8).
 */
public enum ConsoleCommand {

    LOAD_MACHINE_FROM_XML(1, "Load machine configuration from XML"),
    SHOW_MACHINE_SPEC(2, "Show machine specification"),
    SET_MANUAL_CODE(3, "Set manual code configuration"),
    SET_AUTOMATIC_CODE(4, "Set automatic code configuration"),
    PROCESS_INPUT(5, "Process input"),
    RESET_CODE(6, "Reset current code to original"),
    SHOW_HISTORY_AND_STATS(7, "Show history & statistics"),
    EXIT(8, "Exit");

    private final int id;
    private final String description;

    ConsoleCommand(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Finds a command by the numeric option the user typed.
     * Returns null if no such command exists.
     */
    public static ConsoleCommand fromId(int id) {
        for (ConsoleCommand command : values()) {
            if (command.id == id) {
                return command;
            }
        }
        return null;
    }
}
