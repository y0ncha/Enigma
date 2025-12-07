package enigma.console;

import enigma.console.helper.InputParsers;
import enigma.console.helper.Utilities;
import enigma.console.helper.ConsoleValidator;
import enigma.engine.exception.EngineException;
import enigma.engine.exception.InvalidConfigurationException;
import enigma.engine.exception.InvalidMessageException;
import enigma.engine.exception.MachineNotLoadedException;
import enigma.engine.exception.MachineNotConfiguredException;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.spec.MachineSpec;
import enigma.shared.dto.config.CodeConfig;
import enigma.engine.Engine;

import java.util.List;
import java.util.Scanner;

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

    public ConsoleImpl(Engine engine, Scanner scanner) {
        this.enigma = engine;
        this.scanner = scanner;
    }

    public ConsoleImpl(Engine engine) {
        this.enigma = engine;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        Utilities.printInfo("Welcome to the Enigma machine console (Exercise 1)");
        while (!exitRequested) {
            printMenu();
            ConsoleCommand command = readCommandFromUser();
            dispatchCommand(command);
            System.out.println();
        }
        Utilities.printInfo("Goodbye!");
    }
    // =========================
    //  Menu & dispatch
    // =========================

    private void printMenu() {
        System.out.println("========================================");
        System.out.println(" Enigma Machine - Main Menu");
        System.out.println("========================================");
        System.out.println("Please choose an option by number:");
        for (ConsoleCommand cmd : ConsoleCommand.values()) {
            boolean enabled = isCommandEnabled(cmd);
            if (enabled) {
                System.out.printf(" %d. %s%n", cmd.getId(), cmd.getDescription());
            } else {
                System.out.printf(" %d. %s (disabled)%n", cmd.getId(), cmd.getDescription());
            }
        }
        System.out.print("> ");
    }

    /**
     * Returns a human-readable explanation why a command is disabled.
     * Returns null if the command IS enabled.
     */
    private String getDisabledReason(ConsoleCommand command) {
        switch (command) {
            // Command 1 + 8: always enabled
            case LOAD_MACHINE_FROM_XML:
            case EXIT:
                return null;
            // Commands 2, 3, 4, 7: require a valid XML to be loaded first
            case SHOW_MACHINE_SPEC:
            case SET_MANUAL_CODE:
            case SET_AUTOMATIC_CODE:
            case SHOW_HISTORY_AND_STATS:
                if (!machineLoaded) {  return "You must load a valid XML configuration first (Command 1)."; }
                return null;
            // Commands 5, 6: require XML + code configuration (manual/automatic)
            case PROCESS_INPUT:
            case RESET_CODE:
                if (!machineLoaded) { return "You must load a valid XML configuration first (Command 1)."; }
                if (!codeConfigured) { return "You must set a code configuration first (Command 3 or 4)."; }
                return null;
            // Fallback (should not occur)
            default:
                return "This command cannot be used at the moment.";
        }
    }

    /**
     * Returns true if the given command is currently enabled.
     */
    private boolean isCommandEnabled(ConsoleCommand command) {
        // A command is enabled if and only if there is no disabled reason
        return getDisabledReason(command) == null;
    }

    private ConsoleCommand readCommandFromUser() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                ConsoleCommand command = ConsoleValidator.parseCommand(input);
                // 4. Check whether this command is enabled at the moment
                if (!isCommandEnabled(command)) {
                    String reason = getDisabledReason(command);
                    if (reason == null) {
                        reason = "This command is currently disabled.";
                    }
                    Utilities.printError(reason);
                    System.out.print("> ");
                    continue;
                }
                return command;
            } catch (IllegalArgumentException e) {
                Utilities.printError(e.getMessage());
                System.out.print("> ");
            }
        }
    }

    private void dispatchCommand(ConsoleCommand command) {
        if (command == null) {
            Utilities.printError("Invalid command. Please choose a number between 1 and 8.");
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
            case EXIT -> handleExit();
        }
    }


    // =========================
    //  Command 1: Load machine from XML
    // =========================
    /**
     * Command 1: Load machine configuration from XML file.
     * <p>
     * Flow (logic to be implemented later):
     * - ask user for full XML path (may contain spaces)
     * - validate file extension (.xml)
     * - call engine to load & validate machine
     * - if invalid: print clear error (do NOT crash)
     * - if valid: inform user + override previous machine
     * - note: this command is always enabled
     */
    private void handleLoadMachineFromXml() {

        while (true) {
            String path = Utilities.readNonEmptyLine(scanner, "Please enter the full path to the XML file:");
            try {
                enigma.loadMachine(path);
                // If we got here – loading succeeded
                machineLoaded = true;
                codeConfigured = false; // previous code no longer relevant
                Utilities.printInfo("Machine configuration loaded successfully from: " + path);
                return;
            } catch (EngineException e) {
                // Catch all engine exceptions (includes EngineException and its subclasses)
                Utilities.printError("Failed to load machine from XML file: " + e.getMessage());
                // Do not override any existing machine; upon failure we keep prior state
                if (!Utilities.askUserToRetry(scanner, "Do you want to try a different path? (Y/N): ")) {
                    return; // back to menu
                }
                // else - loop and ask again
            }
        }
    }

    // =========================
    //  Command 2: Show machine specification
    // =========================

    /**
     * Command 2: Show machine specification of the last successfully loaded machine.
     * Prints:
     *  - total rotors in system
     *  - total reflectors
     *  - total processed messages since last XML load
     *  - original code configuration (if exists)
     *  - current code configuration (if exists)
     * Code configuration is printed in the compact format defined in the exercise,
     * delegated to CodeConfig.toString().
     */
    private void handleShowMachineSpecification() {

        if (!machineLoaded) {
            Utilities.printError("No machine is currently loaded. Please load an XML file first (Command 1).");
            return;
        }
        try {
            MachineSpec machineSpec = enigma.getMachineSpec();
            System.out.println("========================================");
            System.out.println(" Enigma Machine - Specification");
            System.out.println("========================================");
            System.out.println("Number of Reflectors         : " + machineSpec.getTotalReflectors());
            System.out.println("Number of Rotors             : " + machineSpec.getTotalRotors());

            long totalProcessedMessages = enigma.getTotalProcessedMessages();
            System.out.println("Total processed messages     : " + totalProcessedMessages);

            // Description of the original code configuration (if it exists; the most recent one set by command 3 or 4)
            CodeConfig originalCode = enigma.getCurrentCodeConfig();
            if (originalCode != null) {
                System.out.println("Original code configuration  : " + originalCode);
            } else {
                System.out.println("Original code configuration  : <not set yet>");
            }
            // Description of the current code configuration (if it exists; it may differ from the original configuration due to input processing – command 5)
            CodeConfig currentCode = enigma.getCurrentCodeConfig();
            if (currentCode != null) {
                System.out.println("Current code configuration   : " + currentCode);
            } else {
                System.out.println("Current code configuration   : <not set yet>");
            }
        } catch (EngineException e) {
            // Catch all engine exceptions (machine not loaded, machine not configured, etc.)
            Utilities.printError("Failed to show machine specification: " + e.getMessage());
        }
    }


    // =========================
    //  Command 3: Manual code selection
    // =========================

    /**
     * Command 3: Let user manually choose current code configuration.
     * Flow:
     *  - read rotors list as comma-separated decimal ids (e.g. "23,542,231,545")
     *  - read initial positions as continuous string (e.g. "ABCD")
     *  - show reflectors as numeric menu (1..N) and read decimal choice
     *  *  - perform basic input validation (numbers where expected, lengths match, etc.)
     *  - delegate deeper validation to the engine
     *  - on error: print clear message and let the user decide whether to retry or return to main menu
     *  - on success: update engine with new code and print compact format
     */
    private void handleSetManualCode() {
        if (!machineLoaded) {
            Utilities.printError("No machine is currently loaded. Please load an XML file first (Command 1).");
            return;
        }

        boolean keepTrying = true;
        while (keepTrying) {
            List<Integer> rotorIds;
            List<Character> initialPositions;
            String reflectorId;
            // =========================
            // 1) Rotor list (loop until valid or user exits)
            // =========================
            while (true) {
                System.out.println("Available rotors: " + enigma.getMachineSpec().getTotalRotors());
                String rotorsLine = Utilities.readNonEmptyLine(scanner,
                        "Enter rotor IDs as a comma-separated list (e.g. 23,542,231,545):");
                try {
                    rotorIds = InputParsers.parseRotorIds(rotorsLine);
                    // Parsing validation passed → move to next stage
                    break;
                } catch (IllegalArgumentException e) {
                    Utilities.printError(e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with a different rotor list? (Y/N): ")) {
                        return; // back to main menu
                    }
                }
            }
            // =========================
            // 2) Initial positions (loop until valid or user exits)
            // =========================
            while (true) {
                String positions = Utilities.readNonEmptyLine(scanner,
                        "Enter initial positions as a continuous sequence of characters (e.g. ABCD):");
                try {
                    ConsoleValidator.ensurePositionsLengthMatches(positions, rotorIds.size());
                } catch (IllegalArgumentException e) {
                    Utilities.printError(e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with different positions? (Y/N): ")) {
                        return;
                    }
                    continue;
                }
                initialPositions = InputParsers.buildInitialPositions(positions);
                // Validate that positions characters belong to machine alphabet (format-level)
                try {
                    ConsoleValidator.validatePositionsInAlphabet(enigma.getMachineSpec(), initialPositions);
                } catch (IllegalArgumentException e) {
                    Utilities.printError(e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with different positions? (Y/N): ")) {
                        return;
                    }
                    continue;
                }
                // Format conversion completed → move to next stage
                break;
            }
            // =========================
            // 3) Reflector choice (loop until valid or user exits)
            // =========================
            int reflectorsCount = enigma.getMachineSpec().getTotalReflectors();
            if (reflectorsCount <= 0) {
                Utilities.printError("No reflectors are defined in the current machine configuration.");
                return;
            }
            while (true) {
                System.out.println("Available reflectors:");
                for (int i = 1; i <= reflectorsCount; i++) {
                    System.out.println(" " + i + ". " + InputParsers.toRoman(i));
                }
                int reflectorChoice = Utilities.readInt(scanner,
                        "Choose reflector by number (1-" + reflectorsCount + "): ");
                try {
                    ConsoleValidator.ensureReflectorChoiceInRange(reflectorChoice, reflectorsCount);
                } catch (IllegalArgumentException e) {
                    Utilities.printError(e.getMessage());
                    if (!Utilities.askUserToRetry(scanner, "Do you want to try again with a different reflector? (Y/N): ")) {
                        return;
                    }
                    continue;
                }
                reflectorId = InputParsers.toRoman(reflectorChoice);
                // Format validation passed → move to next stage
                break;
            }
            // =========================
            // 4) Build CodeConfig and delegate to engine
            //    Engine may still reject configuration (range, duplicates, etc.)
            // =========================
            try {
                CodeConfig config = new CodeConfig(rotorIds, initialPositions, reflectorId);
                enigma.configManual(config);
                codeConfigured = true;
                Utilities.printInfo("Manual code configuration was set successfully.");
                CodeConfig currentConfig = enigma.getCurrentCodeConfig();
                if (currentConfig != null) {
                    System.out.println("Current code: " + currentConfig);
                }
                keepTrying = false; // success – exit command
            } catch (InvalidConfigurationException e) {
                // Catch configuration validation errors from engine
                Utilities.printError("Invalid code configuration: " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner, "Do you want to try again and fix the configuration? (Y/N): ")) {
                    return;
                }
                // if user chose YES – outer while(keepTrying) repeats from the beginning
            }
        }
    }

    // =========================
    //  Command 4: Automatic code selection
    // =========================

    /**
     * Command 4: Automatically generate random valid code configuration.
     * - call engine to generate random rotors, positions, reflector
     * - print the chosen configuration in compact format
     * - configuration becomes active in the machine
     * Enabled only after valid XML is loaded.
     */
    private void handleSetAutomaticCode() {
        try {
            // Delegate random code generation to the engine.
            enigma.configRandom();
            // Mark that a valid code configuration is now active.
            codeConfigured = true;
            Utilities.printInfo("Automatic code configuration was generated successfully.");
            System.out.println("Current code: " + enigma.getCurrentCodeConfig().toString());
        } catch (EngineException e) {
            // Catch all engine exceptions (machine not loaded, etc.)
            Utilities.printError("Failed to generate automatic code configuration: " + e.getMessage());
        }
    }


    // =========================
    //  Command 5: Process input
    // =========================

    /**
     * Command 5: Process user input string through the machine.
     * Flow:
     * - ensure machine is loaded and a code (manual/automatic) was already set
     * - ask user for input string
     * - validate all characters are from machine alphabet (case-insensitive)
     * - call engine to process and measure duration (nano-seconds)
     * - print: original input, processed output and duration
     * - note: rotors remain in their new positions (no auto reset)
     */
    private void handleProcessInput() {
        boolean keepTrying = true;
        while (keepTrying) {
            try {
                // 1. Read input from user
                String originalInput = Utilities.readNonEmptyLine(scanner,
                        "Enter the text you want to process (only characters from the machine alphabet):");

                // Normalize to upper-case to make input case-insensitive
                String normalizedInput = originalInput.toUpperCase();

                // Validate input characters belong to machine alphabet before processing
                ConsoleValidator.validateInputInAlphabet(enigma.getMachineSpec(), normalizedInput);

                // 2. Process input via engine and measure duration
                //    Engine will validate that characters are in the machine alphabet
                long startNano = System.nanoTime();
                ProcessTrace trace = enigma.process(normalizedInput);
                long endNano = System.nanoTime();
                long duration = endNano - startNano;
                // Convert to milliseconds
                double millis = duration / 1_000_000.0;
                String processedOutput = trace.output();
                
                // 3. Print results
                System.out.println("Original input : <" + originalInput + ">");
                System.out.println("Processed output: <" + processedOutput + ">");
                System.out.println("Processing time: " + duration + " nanoseconds"
                        + " (" + String.format("%.3f", millis) + " ms)");
                // Rotors remain in their new positions (no reset here)
                keepTrying = false; // success → exit command
            } catch (EngineException e) {
                // Catch all engine exceptions (invalid message, machine not configured, etc.)
                Utilities.printError("Failed to process input: " + e.getMessage());
                if (!Utilities.askUserToRetry(scanner,
                        "Do you want to try again with a different input string? (Y/N): ")) {
                    return;
                }
            }
        }
    }


    // =========================
    //  Command 6: Reset current code
    // =========================
    /**
     * Command 6: Reset current rotors to original code configuration.
     * - enabled only after XML is loaded AND a code was set (3 or 4)
     * - use engine.getOriginalCodeConfig() to retrieve the saved initial config
     * - call engine.codeManual(...) to re-apply it
     * - print resulting configuration in compact format
     */
    private void handleResetCode() {
        // Get the current configuration from the engine
        CodeConfig current = enigma.getCurrentCodeConfig();
        if (current == null) {
            Utilities.printError("No last configuration was found. Cannot reset.");
            return;
        }
        try {
            // Apply the current code again (reinitializes rotors)
            enigma.configManual(current);
            Utilities.printInfo("Code was reset to the current configuration.");
            System.out.println("Current code: " + current);
            // Print the resulting (current) code in compact format
        } catch (EngineException e) {
            // Catch all engine exceptions
            Utilities.printError("Failed to reset code: " + e.getMessage());
        }
    }
    // =========================
    //  Command 7: History & statistics
    // =========================

    /**
     * Command 7: Show machine history & statistics.
     * <p>
     * For each original code configuration (set by 3 or 4):
     * - print the code in compact format
     * - under it, print all processed messages:
     *   #. &lt;input&gt; --> &lt;output&gt; (n nano-seconds)
     * where # is running index starting from 1.
     */
    private void handleShowHistoryAndStatistics() {
        // TODO
    }

    // =========================
    //  Command 8: Exit
    // =========================

    /**
     * Command 8: Exit application.
     * Sets a flag so that the main loop in run() stops.
     */
    private void handleExit() {
        exitRequested = true;
    }
}
