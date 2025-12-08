package enigma.console;

/**
 * All supported console commands for the Enigma console interface.
 *
 * <p><b>Module:</b> enigma-console</p>
 *
 * <h2>Purpose</h2>
 * <p>This enum defines the available user commands in the console menu,
 * mapping numeric IDs (1-8) to specific operations.</p>
 *
 * <h2>Command Flow</h2>
 * <ul>
 *   <li><b>1. LOAD_MACHINE_FROM_XML:</b> Load machine spec from XML (always enabled)</li>
 *   <li><b>2. SHOW_MACHINE_SPEC:</b> Display machine data (requires loaded machine)</li>
 *   <li><b>3. SET_MANUAL_CODE:</b> Configure rotors manually (requires loaded machine)</li>
 *   <li><b>4. SET_AUTOMATIC_CODE:</b> Configure rotors randomly (requires loaded machine)</li>
 *   <li><b>5. PROCESS_INPUT:</b> Encrypt/decrypt message (requires configured machine)</li>
 *   <li><b>6. RESET_CODE:</b> Return to original positions (requires configured machine)</li>
 *   <li><b>7. SHOW_HISTORY_AND_STATS:</b> Display processing history (requires loaded machine)</li>
 *   <li><b>8. EXIT:</b> Terminate console application (always enabled)</li>
 * </ul>
 *
 * @since 1.0
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

    /**
     * Create a console command with numeric ID and description.
     *
     * @param id numeric identifier (1-8) displayed in menu
     * @param description user-friendly command description
     */
    ConsoleCommand(int id, String description) {
        this.id = id;
        this.description = description;
    }

    /**
     * Get the numeric command ID.
     *
     * @return command ID (1-8)
     */
    public int getId() {
        return id;
    }

    /**
     * Get the command description for display.
     *
     * @return user-friendly command description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Find a command by its numeric ID.
     *
     * <p>Used to map user input (numeric menu choice) to the corresponding command.</p>
     *
     * @param id numeric command ID (1-8)
     * @return matching ConsoleCommand, or null if ID is invalid
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
