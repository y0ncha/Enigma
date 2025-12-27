package enigma.console;

import enigma.console.helper.InputParsers;
import enigma.console.helper.Utilities;
import enigma.console.helper.ConsoleValidator;
import enigma.engine.exception.EngineException;
import enigma.engine.exception.InvalidConfigurationException;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.dto.config.CodeConfig;
import enigma.engine.Engine;
import enigma.engine.EngineValidator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import enigma.shared.state.CodeState;
/**
 * Concrete console implementation for Exercise 1.
 * Responsible for:
 * - Showing the main menu
 * - Format-level input validation (parsing, empty checks, basic type conversion)
 * - Calling the engine for each command (engine performs semantic validation)
 * - Catching and displaying user-friendly error messages from engine
 * - Printing user-friendly messages
 */
public class ConsoleImpl implements Console {

    private final Engine enigma;
    private final Scanner scanner;
    private boolean machineLoaded = false;
    private boolean codeConfigured = false;
    private boolean exitRequested = false;

    /**
     * Construct console with provided engine and scanner.
     *
     * <p>Used for testing or when input source needs to be controlled.</p>
     *
     * @param engine the Enigma engine for business logic
     * @param scanner the scanner for reading user input
     * @since 1.0
     */
    public ConsoleImpl(Engine engine, Scanner scanner) {
        this.enigma = engine;
        this.scanner = scanner;
    }

    /**
     * Construct console with provided engine and System.in scanner.
     *
     * <p>Standard constructor for normal console operation with keyboard input.</p>
     *
     * @param engine the Enigma engine for business logic
     * @since 1.0
     */
    public ConsoleImpl(Engine engine) {
        this.enigma = engine;
        this.scanner = new Scanner(System.in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        System.out.println();
        System.out.println("Welcome to the Enigma machine console (Exercise 1)");
        System.out.println("===============================================================");
        System.out.println();

        while (!exitRequested) {
            printMenu();
            ConsoleCommand command = readCommandFromUser();
            dispatchCommand(command);
            System.out.println();
        }

        System.out.println("===============================================================\n");
        System.out.print(enigma.getMachineDetails());
        System.out.println();
        System.out.println("Goodbye!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runTest(String xmlPath) {
        System.out.println();
        System.out.println("Welcome to the Enigma machine console (Exercise 1) - Test Mode");
        System.out.println("===============================================================");

        try {
            enigma.loadMachine(xmlPath);
            machineLoaded = true;
            codeConfigured = false; // previous code no longer relevant
            System.out.println("Machine specification loaded successfully from : " + xmlPath);
            System.out.println();
        } catch (Exception e) {
            System.out.println("File Loading failed : " + e.getMessage());
            return;
        }

        while (!exitRequested) {
            printMenu();
            ConsoleCommand command = readCommandFromUser();
            dispatchCommand(command);
            System.out.println();
        }

        System.out.println("===============================================================\n");
        System.out.print(enigma.getMachineDetails());
        System.out.println();
        System.out.println("Goodbye!");
    }

// ---------------------------------------------------------
// Menu & dispatch
// ---------------------------------------------------------

    /**
     * Display the main menu with all available commands.
     *
     * <p>Shows enabled commands normally and disabled commands with "(disabled)"
     * suffix. Commands are numbered 1-N matching their command IDs.</p>
     */
    private void printMenu() {
        printSectionHeader("Enigma Machine - Main Menu");
        System.out.println("Please choose an option by number : ");
        for (ConsoleCommand cmd : ConsoleCommand.values()) {
            boolean enabled = isCommandEnabled(cmd);
            if (enabled) {
                System.out.printf(" %d. %s%n", cmd.getId(), cmd.getDescription());
            }
            else {
                System.out.printf(" %d. %s (disabled)%n", cmd.getId(), cmd.getDescription());
            }
        }
        System.out.print("> ");
    }

    /**
     * Get explanation for why a command is disabled.
     *
     * @param command the command to check
     * @return user-friendly reason if disabled, null if enabled
     */
    private String getDisabledReason(ConsoleCommand command) {
        switch (command) {
            // Command 1 + 8: always enabled
            case LOAD_MACHINE_FROM_XML:
            case EXIT:
            case LOAD_MACHINE_STATE_FROM_FILE:
                return null;
            // Commands 2, 3, 4, 7: require a valid XML to be loaded first
            case SHOW_MACHINE_SPEC:
            case SET_MANUAL_CODE:
            case SET_AUTOMATIC_CODE:
            case SHOW_HISTORY_AND_STATS:
            case SAVE_CURRENT_MACHINE_STATE_TO_FILE:
                if (!machineLoaded) {  return "You must load a valid XML | File configuration first (Command 1 | 10)"; }
                return null;
            // Commands 5, 6: require XML + code configuration (manual/automatic)
            case PROCESS_INPUT:
            case RESET_CODE:
                if (!machineLoaded) { return "You must load a valid XML | File configuration first (Command 1 | 10)"; }
                if (!codeConfigured) { return "You must set a code configuration first (Command 3 or 4)"; }
                return null;
            // Fallback (should not occur)
            default:
                return "This command cannot be used at the moment";
        }
    }

    /**
     * Check if a command is currently enabled.
     *
     * @param command the command to check
     * @return true if enabled, false if disabled
     */
    private boolean isCommandEnabled(ConsoleCommand command) {
        // A command is enabled if and only if there is no disabled reason
        return getDisabledReason(command) == null;
    }

    /**
     * Reads a command from the user input in a loop until a valid and enabled command is provided.
     * <p>
     * The method performs the following steps:
     * - Reads a line of input from the user.
     * - Attempts to parse the input into a {@code ConsoleCommand} using {@code ConsoleValidator.parseCommand}.
     * - Checks if the parsed command is currently enabled using {@code isCommandEnabled}.
     * - If the command is disabled, displays an error message with the reason and prompts the user to try again.
     * - If the input is invalid, catches the exception, displays an error message, and prompts the user to enter a valid command.
     * - Returns the valid and enabled {@code ConsoleCommand} once successfully parsed and validated.
     *
     * @return the valid and enabled {@link ConsoleCommand} entered by the user
     * @throws IllegalArgumentException if the input cannot be parsed into a valid command
     */
    private ConsoleCommand readCommandFromUser() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                ConsoleCommand command = ConsoleValidator.parseCommand(input);
                // 4. Check whether this command is enabled at the moment
                if (!isCommandEnabled(command)) {
                    String reason = getDisabledReason(command);
                    if (reason == null) {
                        reason = "This command is currently disabled";
                    }
                    // print headline and reason on same line
                    System.out.println("This command is currently disabled : " + reason);
                    System.out.println("Please choose another command :");
                    System.out.print("> ");
                    continue;
                }
                return command;
            }
            catch (IllegalArgumentException e) {
                System.out.print("Error : " + e.getMessage());
                System.out.println("Enter a valid command number (1-10) :");
                System.out.print("> ");
            }
        }
    }

    /**
     * Dispatch command to appropriate handler method.
     *
     * @param command the command to execute
     */
    private void dispatchCommand(ConsoleCommand command) {
        if (command == null) {
            System.out.println("Invalid command. Please choose a number between 1 and 8");
            return;
        }
        switch (command) {
            case LOAD_MACHINE_FROM_XML -> handleLoadMachineFromXml();
            case SHOW_MACHINE_SPEC -> handleShowMachineSpecification();
            case SET_MANUAL_CODE -> handleSetManualCode();
            case SET_AUTOMATIC_CODE -> handleSetAutomaticCode();
            case PROCESS_INPUT -> handleProcessInput();
            case RESET_CODE -> handleResetCode();
            case SHOW_HISTORY_AND_STATS -> handleShowHistoryAndStatistics();
            case SAVE_CURRENT_MACHINE_STATE_TO_FILE -> handleSaveSnapshot();
            case LOAD_MACHINE_STATE_FROM_FILE -> handleLoadSnapshot();
            case EXIT -> handleExit();
        }
    }

// ---------------------------------------------------------
// Command 1: Load machine from XML
// ---------------------------------------------------------

    /**
     * Handle command 1: Load machine configuration from XML file.
     *
     * <p>Prompts user for file path, validates extension, and loads
     * machine specification through the engine.</p>
     */
    private void handleLoadMachineFromXml() {

        while (true) {
            String path = Utilities.readNonEmptyLine(scanner, "Please enter the full path to the XML file :");
            try {
                enigma.loadMachine(path);
                // If we got here – loading succeeded
                machineLoaded = true;
                codeConfigured = false; // previous code no longer relevant
                System.out.println("Machine specification loaded successfully from : " + path);
                return;
            }
            catch (Exception e) {
                // Catch all engine exceptions (includes EngineException and its subclasses)
                System.out.println("File Loading failed : " + e.getMessage());
                // Do not override any existing machine; upon failure we keep prior state
                if (!Utilities.askUserToRetry(scanner, "Do you want to try a different path? (Y/N) : ")) {
                    return; // back to menu
                }
                // else - loop and ask again
            }
        }
    }

// ---------------------------------------------------------
// Command 2: Show machine specification
// ---------------------------------------------------------

    /**
     * Handle command 2: Display machine specification and current state.
     *
     * <p>Shows rotor count, reflector count, processed message count,
     * and current configuration details.</p>
     */
    private void handleShowMachineSpecification() {

        try {
            printSectionHeader("Enigma Machine - Specification");
            System.out.println(enigma.machineData());
        }
        catch (EngineException e) {
            System.out.println("Failed to show machine specification : " + e.getMessage());
        }
    }

// ---------------------------------------------------------
// Command 3: Manual code selection
// ---------------------------------------------------------

    /**
     * Handle command 3: Manual code configuration.
     *
     * <p>Prompts user for rotor IDs, positions, reflector selection,
     * and plugboard configuration. Validates input and applies
     * configuration through the engine.</p>
     */
    private void handleSetManualCode() {

        boolean keepTrying = true;
        int reflectorsCount = enigma.getMachineSpec().getTotalReflectors();
        List<Integer> rotorIds;
        List<Character> positionsLst;
        String reflectorId;
        String plugStr;
        while (keepTrying) {

// ---------------------------------------------------------
// 1) Rotor list (loop until valid or user exits)
// ---------------------------------------------------------
            while (true) {
                int total = enigma.getMachineSpec().getTotalRotors();
                int rotorsInUse = enigma.getMachineSpec().getRotorsInUse();
                String available = IntStream.rangeClosed(1, total)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(", "));
                System.out.println();
                String rotorsLine = Utilities.readNonEmptyLine(scanner,
                        "Enter exactly " + rotorsInUse + " rotor IDs as a comma-separated list (e.g. in \"3,2,1\" → 3 is leftmost, 1 is rightmost)\n" +
                                "Allowed rotor IDs : " + available
                        );

                try {
                    // Parsing validation
                    rotorIds = InputParsers.parseRotorIds(rotorsLine);
                    // Engine-level validation
                    EngineValidator.validateRotors(enigma.getMachineSpec(), rotorIds);
                    // → move to next stage
                    break;
                }
                catch (IllegalArgumentException e) {
                    // Known validation error
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner,
                            "Do you want to try again with a different rotor list? (Y/N) : ")) {
                        return; // back to main menu
                    }
                } catch (Exception e) {
                    // Unknown / unexpected error
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner,
                            "Try again? (Y/N) : ")) {
                        return; // back to main menu
                    }
                }
            }

// ---------------------------------------------------------
// 2) Initial positions (loop until valid or user exits)
// ---------------------------------------------------------
            while (true) {
                String positions = Utilities.readNonEmptyLine(scanner,
                        "Enter initial positions as a continuous sequence of characters (e.g. ABCD)\n" +
                                "Allowed letters: " + enigma.getMachineSpec().getAlphabet());
                try {
                    positionsLst = InputParsers.parsePositions(positions);
                    EngineValidator.validatePositions(enigma.getMachineSpec(), positionsLst);
                    // → move to next stage
                    break;
                }
                catch (IllegalArgumentException e) {
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with different positions? (Y/N) : ")) {
                        return;
                    }
                } catch (Exception e) {
                    // Unknown / unexpected error
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner,
                            "An unexpected error occurred. Try again? (Y/N) : ")) {
                        return; // back to main menu
                    }
                }
            }

// ---------------------------------------------------------
// 3) Reflector choice (loop until valid or user exits)
// ---------------------------------------------------------
            while (true) {
                System.out.println("Available reflectors :");
                for (int i = 1; i <= reflectorsCount; i++) {
                    System.out.println(" " + i + ". " + InputParsers.toRoman(i));
                }
                System.out.println("Choose reflector by number (1-" + reflectorsCount + ") : ");
                System.out.print("> ");
                int reflectorChoice = Utilities.readInt(scanner, "");
                try {
                    ConsoleValidator.ensureReflectorChoiceInRange(reflectorChoice, reflectorsCount);
                    reflectorId = InputParsers.toRoman(reflectorChoice);
                    EngineValidator.validateReflectorExists(enigma.getMachineSpec(), reflectorId);
                    break;
                }
                catch (IllegalArgumentException e) {
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with a different reflector? (Y/N) : ")) {
                        return;
                    }
                }
                catch (Exception e) {
                    // Unknown / unexpected error
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner,
                            "An unexpected error occurred. Try again? (Y/N) : ")) {
                        return; // back to main menu
                    }
                }
            }
// ---------------------------------------------------------
// 4) Plugboard choice (loop until valid or user exits)
// ---------------------------------------------------------
            while (true) {
                System.out.println("Enter plug pairs as a continuous string (even length), e.g. ABKC (A ↔ B, K ↔ C)");
                System.out.println("Each letter may appear at most once, and a letter cannot be paired with itself (press Enter for no plugs) : ");
                System.out.print("> ");

                String plugsInput = scanner.nextLine();
                plugStr = plugsInput.trim().toUpperCase();
                try {
                    EngineValidator.validatePlugs(plugStr, enigma.getMachineSpec().getAlphabet());
                    break;
                }
                catch (IllegalArgumentException e) {
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with a different Plugboard? (Y/N) : ")) {
                        return;
                    }
                }
                catch (Exception e) {
                    // Unknown / unexpected error
                    System.out.println("Error : " + e.getMessage());
                    if (!Utilities.askUserToRetry(scanner,
                            "An unexpected error occurred. Try again? (Y/N) : ")) {
                        return; // back to main menu
                    }
                }
            }
// ---------------------------------------------------------
// 5) Build CodeConfig and delegate to engine
// ---------------------------------------------------------
            try {
                CodeConfig config = new CodeConfig(rotorIds, positionsLst, reflectorId, plugStr);
                enigma.configManual(config);
                codeConfigured = true;
                System.out.println();
                System.out.println("Manual code configuration was set successfully");
                CodeState currentConfig = enigma.machineData().curCodeState();
                if (currentConfig != null) {
                    System.out.println("Current code : " + currentConfig);
                }
                keepTrying = false; // success – exit command
            }
            catch (InvalidConfigurationException e) {
                // Catch configuration validation errors from engine
                System.out.println("Invalid code configuration : " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner, "Do you want to try again and fix the configuration? (Y/N) : ")) {
                    return;
                }
            }
            catch (Exception e) {
                // Unknown / unexpected error
                System.out.println("Error : " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner, "An unexpected error occurred. Try again? (Y/N) : ")) {
                    return; // back to main menu
                }
            }
        }
    }

// ---------------------------------------------------------
// Command 4: Automatic code selection
// ---------------------------------------------------------

    /**
     * Handle command 4: Generate random code configuration.
     *
     * <p>Delegates to engine to generate random valid configuration
     * including rotor selection, positions, and reflector choice.</p>
     */
    private void handleSetAutomaticCode() {
        try {
            // Delegate random code generation to the engine.
            enigma.configRandom();
            // Mark that a valid code configuration is now active.
            codeConfigured = true;
            System.out.println("Automatic code configuration was generated successfully");
            CodeState currentConfig = enigma.machineData().curCodeState();
            if (currentConfig != null) {
                System.out.println("Current code : " + currentConfig);
            }
        } catch (EngineException e) {
            // Catch all engine exceptions (machine not loaded, etc.)
            System.out.println("Failed to generate automatic code configuration : " + e.getMessage());
        }
    }

// ---------------------------------------------------------
// Command 5: Process input
// ---------------------------------------------------------

    /**
     * Handle command 5: Process input message through the machine.
     *
     * <p>Prompts user for input string, validates characters against
     * alphabet, and processes through configured machine. Displays
     * original input, processed output, and character-by-character trace.</p>
     *
     * - note: rotors remain in their new positions (no auto reset)
     */
    private void handleProcessInput() {
        boolean keepTrying = true;
        while (keepTrying) {
            try {
                // 1. Read input from user
                String originalInput = Utilities.readNonEmptyLine(scanner,
                        "Enter the text you want to process, only characters from the machine alphabet : "
                                + enigma.getMachineSpec().getAlphabet());
                // Normalize to upper-case to make input case-insensitive
                String normalizedInput = originalInput.toUpperCase();
                // 2. Process input via engine and measure duration
                // Engine will validate that characters are in the machine alphabet
                EngineValidator.validateInputInAlphabet(enigma.getMachineSpec(), normalizedInput);
                ProcessTrace trace = enigma.process(normalizedInput);
                // Convert to milliseconds
                String processedOutput = trace.output();
                // 3. Print results
                System.out.println("Original input : <" + normalizedInput + ">");
                System.out.println("Processed output : <" + processedOutput + ">");
                // Rotors remain in their new positions (no reset here)
                keepTrying = false; // success → exit command
            } catch (EngineException e) {
                // Catch all engine exceptions (invalid message, machine not configured, etc.)
                System.out.println("Failed to process input : " +e.getMessage());
                if (!Utilities.askUserToRetry(scanner,
                        "Do you want to try again with a different input string? (Y/N) : ")) {
                    return;
                }
            }
        }
    }

// ---------------------------------------------------------
// Command 6: Reset current code
// ---------------------------------------------------------

    /**
     * Handle command 6: Reset rotor positions to original configuration.
     *
     * <p>Returns rotor positions to their values at configuration time.
     * Does not change rotor selection, reflector, or plugboard.</p>
     */
    private void handleResetCode() {
        // Get the current configuration from the engine
        try {
            enigma.reset();
            System.out.println("Code has been reset to original configuration");
        }
        catch (EngineException e) {
            System.out.println("Failed to reset code : " + e.getMessage());
        }
    }
// ---------------------------------------------------------
// Command 7: History & statistics
// ---------------------------------------------------------

    /**
     * Handle command 7: Display processing history and statistics.
     *
     * <p>Shows all processed messages grouped by original code configuration,
     * including input, output, and processing duration for each message.</p>
     */
    private void handleShowHistoryAndStatistics() {
        try {
            printSectionHeader("Enigma Machine - History");
            System.out.println(enigma.history());
        }
        catch (Exception e) {
            System.out.println("Failed to show history and statistics : " + e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // Section header printer
    // ---------------------------------------------------------

    /**
     * Print a single-line bracketed section header with surrounding blank lines.
     * Example output:
     * ----------[ Enigma Machine - Main Menu ]----------
     */
    private void printSectionHeader(String title) {
        System.out.println("----------[ " + title + " ]----------");
        System.out.println();
    }

    // ----------[ Command 8: Exit ]----------

    /**
     * Handle command 8: Exit application.
     *
     * <p>Terminates the engine and sets exit flag to stop the main loop.</p>
     */
    private void handleExit() {
        enigma.terminate();
        exitRequested = true;
    }
    // ----------[ Command 9: Save current machine state to a JSON file ]----------
    /**
     * Handle command 9: Save machine state to JSON file.
     *
     * <p>Prompts user for file path and delegates to engine to save
     * complete machine state including specification and configuration.</p>
     */
    private void handleSaveSnapshot() {
        while (true) {
            String basePath = Utilities.readNonEmptyLine(
                    scanner,
                    "Enter full path (folder, without extension) for the snapshot file :");
            try {
                // We know enigma is EngineImpl, so we can safely cast here
                enigma.saveSnapshot(basePath);
                System.out.println("Machine snapshot was saved successfully.");
                return;
            } catch (EngineException e) {
                System.out.println("Error : " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner,
                        "Do you want to try again with a different path? (Y/N): ")) {
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error : " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner,
                        "An unexpected error occurred. Try again? (Y/N) : ")) {
                    return;
                }
            }
        }
    }

    // [ Command 10: Load machine state from a previously saved JSON snapshot file ]
    /**
     * Handle command 10: Load machine state from JSON snapshot file.
     *
     * <p>Prompts user for file path and delegates to engine to restore
     * complete machine state from previously saved snapshot.</p>
     */
    private void handleLoadSnapshot() {
        while (true) {
            String basePath = Utilities.readNonEmptyLine(
                    scanner,
                    "Enter full path (folder + file name, without extension) of the snapshot to load :");
            try {
                enigma.loadSnapshot(basePath);
                machineLoaded = true;
                if (enigma.machineData().curCodeState() != null) {
                    codeConfigured = true;
                }
                System.out.println("Machine snapshot loaded successfully.");
                return;
            } catch (EngineException e) {
                System.out.println("Error : " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner,
                        "Do you want to try again with a different path? (Y/N): ")) {
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error : " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner,
                        "An unexpected error occurred. Try again? (Y/N) : ")) {
                    return;
                }
            }
        }
    }
}

