# enigma-console

**Module Purpose**: User interaction layer providing command-line interface for the Enigma machine.

## Overview

This module provides a console-based user interface for interacting with the Enigma machine. It handles user input, displays output, and delegates all business logic to the engine. The console is responsible for user experience, not encryption logic.

## Module Layer

**Layer**: Presentation/UI Layer  
**Dependencies**: enigma-engine, enigma-shared  
**Used By**: End users via command-line

## Responsibilities

### What This Module DOES
-  Display interactive menu
-  Read and parse user commands
-  Perform **format-level validation** (parsing, basic type checks)
-  Call engine operations (delegate business logic)
-  Display results and error messages in user-friendly format
-  Print machine data, history, and traces
-  Manage menu enabling/disabling based on state
-  Handle retry loops for invalid input

### What This Module DOES NOT DO
-  Validate machine specifications (loader's responsibility)
-  Validate semantic rules (engine's responsibility - alphabet membership, rotor existence)
-  Implement encryption logic (machine's responsibility)
-  Maintain history or state (engine's responsibility)
-  Parse XML (loader's responsibility)
-  Create machine components (engine's factory responsibility)

## Key Components

### Console (`ConsoleImpl`)
**Purpose**: Main console implementation with menu loop and command dispatch.

**State Management**:
- `enigma`: Reference to Engine instance
- `scanner`: Input scanner for user interaction
- `machineLoaded`: Track if valid XML loaded
- `codeConfigured`: Track if code configured
- `exitRequested`: Control main loop

**Main Loop**:
```java
public void run() {
    while (!exitRequested) {
        printMenu();
        ConsoleCommand command = readCommandFromUser();
        dispatchCommand(command);
    }
}
```

### ConsoleCommand (Enum)
**Purpose**: Define available commands with IDs and descriptions.

**Commands**:
1. **LOAD_MACHINE_FROM_XML** - Load machine specification
2. **SHOW_MACHINE_SPEC** - Display machine configuration
3. **SET_MANUAL_CODE** - Configure code manually
4. **SET_AUTOMATIC_CODE** - Configure code randomly
5. **PROCESS_INPUT** - Encrypt/decrypt message
6. **RESET_CODE** - Reset to original positions
7. **SHOW_HISTORY_AND_STATS** - Display processing history
8. **EXIT** - Exit application

**Command Enabling**:
- Commands 1 & 8: Always enabled
- Commands 2, 3, 4, 7: Require machine loaded
- Commands 5 & 6: Require machine loaded AND code configured

### ConsoleValidator
**Purpose**: Format-level input validation (no semantic checks).

**Validation Methods**:

#### Command Validation
```java
static ConsoleCommand parseCommand(String input, int minId, int maxId)
```
- Parses integer command ID
- Validates ID in range
- Throws `IllegalArgumentException` on failure

#### Rotor List Validation
```java
static void ensureRotorsLengthMatches(List<Integer> rotorIds, int expected)
```
- Checks rotor count matches requirement
- Format check only (engine validates rotor existence)

#### Positions Validation
```java
static void ensurePositionsLengthMatches(String positions, int expectedCount)
```
- Checks position string length
- Validates all characters are A-Z (format check)
- Does NOT validate alphabet membership (engine does this)

#### Reflector Validation
```java
static void ensureReflectorChoiceInRange(int choice, int availableCount)
```
- Validates reflector choice is in available range
- Format check only (engine validates reflector existence)

#### Plugboard Validation
```java
static void validatePlugboardFormat(String plugboard)
```
- Validates even length
- Format check only (engine validates duplicates, self-mapping, alphabet membership)

**Key Principle**: Console validates **format**, engine validates **semantics**.

### InputParsers
**Purpose**: Parse user input into structured types.

**Parsing Methods**:

#### Parse Rotor IDs
```java
static List<Integer> parseRotorIds(String input)
```
- Splits comma-separated IDs
- Converts to integers
- Rejects empty parts (e.g., "1,,3")
- Returns left→right list

#### Parse Positions
```java
static List<Character> parsePositions(String input)
```
- Converts string to character list
- Returns left→right list

#### Parse Reflector Choice
```java
static String parseReflectorChoice(int choice, List<ReflectorSpec> reflectors)
```
- Maps 1-based user choice to reflector ID
- Returns reflector ID string

### Utilities
**Purpose**: Helper methods for console output formatting.

**Methods**:
- `printInfo(String)`: Print informational message
- `printError(String)`: Print error message
- `printSuccess(String)`: Print success message
- Display formatting helpers

## Validation Boundaries

### Console Validates (Format):
- ✅ Command is valid integer in range
- ✅ Rotor list can be parsed as comma-separated integers
- ✅ Position string is all A-Z characters
- ✅ Position string length matches rotor count
- ✅ Reflector choice is integer in range
- ✅ Plugboard is even length

### Console Does NOT Validate (Semantics):
- ❌ Rotor IDs exist in machine spec (engine checks)
- ❌ Rotor IDs are unique (engine checks)
- ❌ Reflector ID exists in machine spec (engine checks)
- ❌ Position characters are in alphabet (engine checks)
- ❌ Input characters are in alphabet (engine checks)
- ❌ Plugboard has no duplicates (engine checks)
- ❌ Plugboard has no self-mappings (engine checks)

**Rationale**: Console handles user input format. Engine handles business rules relative to loaded machine spec.

## User Input Flow

### Load Machine (Command 1)
1. Prompt for file path
2. Call `engine.loadMachine(path)`
3. Catch `EngineException` → display error, retry
4. On success: Set `machineLoaded = true`

### Manual Code Configuration (Command 3)
1. Display available rotors from spec
2. Prompt for rotor IDs (comma-separated)
3. Parse and format-validate rotor list
4. Retry on parse errors
5. Prompt for positions (one string, e.g., "ABC")
6. Format-validate positions (A-Z check, length check)
7. Retry on format errors
8. Display available reflectors
9. Prompt for reflector choice (1-based index)
10. Format-validate choice in range
11. Retry on format errors
12. Build `CodeConfig`
13. Call `engine.configManual(config)`
14. Catch `InvalidConfigurationException` → display error, return to menu
15. On success: Set `codeConfigured = true`

### Process Input (Command 5)
1. Prompt for input message
2. Call `engine.process(input)`
3. Catch `InvalidMessageException` → display error, retry
4. Display output and optionally trace details

### Reset (Command 6)
1. Call `engine.reset()`
2. Display success message
3. Machine returns to original positions (rotor selection unchanged)

## Error Handling Strategy

### Format Errors (Console Catches)
- Parse errors (invalid integer, invalid format)
- **Action**: Display error message, retry input loop
- **Exception**: `IllegalArgumentException` from validators/parsers

### Semantic Errors (Engine Throws)
- Invalid configuration (rotor doesn't exist, position not in alphabet)
- Invalid input (character not in alphabet)
- **Action**: Display error message, return to menu
- **Exception**: `InvalidConfigurationException`, `InvalidMessageException`

### State Errors (Engine Throws)
- Machine not loaded
- Machine not configured
- **Action**: Display error message, return to menu (shouldn't happen due to menu enabling)
- **Exception**: `MachineNotLoadedException`, `MachineNotConfiguredException`

## User Numbering Convention

**All user-facing numbering starts at 1.**

### Rotor IDs
- XML/internal: 1, 2, 3, 4, 5
- User sees: 1, 2, 3, 4, 5
- (Already 1-based in XML, no conversion needed)

### Reflector Choices
- XML/internal IDs: I, II, III
- User sees: 1=I, 2=II, 3=III
- Console converts 1-based choice → Roman ID

### Positions
- XML/internal: Character from alphabet (e.g., 'A', 'B', 'C')
- User enters: Character string (e.g., "ABC")
- No conversion needed (already character-based)

## Position Convention

**All position input and display use left→right order:**
- User enters "ABC": A=leftmost rotor, B=middle, C=rightmost
- Display shows "ABC": A=leftmost rotor, B=middle, C=rightmost
- Console builds `CodeConfig` with positions in left→right order
- Engine and machine maintain left→right order throughout

## Display Output

### Machine Specification Display
- Alphabet
- Available rotor IDs and specs
- Available reflector IDs and specs
- Rotors-in-use count

### Processing Output
- Input message
- Output message
- Optionally: Detailed signal traces (rotor stepping, transformations)

### History Display
- Grouped by original code configuration
- Each message shows: input, output, duration
- Summary statistics

## Main Entry Point

```java
public class Main {
    public static void main(String[] args) {
        Engine engine = new EngineImpl();
        Console console = new ConsoleImpl(engine);
        console.run();
    }
}
```

## Usage Example

```
Welcome to the Enigma machine console (Exercise 1)
========================================
 Enigma Machine - Main Menu
========================================
Please choose an option by number:
 1. Load machine configuration from XML file
 2. Show machine specification (disabled)
 3. Set code manually (disabled)
 4. Set code automatically (disabled)
 5. Process input message (disabled)
 6. Reset code to original positions (disabled)
 7. Show history and statistics (disabled)
 8. Exit
> 1

Enter path to XML file: enigma.xml
✓ Machine loaded successfully!

 1. Load machine configuration from XML file
 2. Show machine specification
 3. Set code manually
 4. Set code automatically
 5. Process input message (disabled)
 6. Reset code to original positions (disabled)
 7. Show history and statistics
 8. Exit
> 3

Available rotors: 1, 2, 3, 4, 5
Enter rotor IDs (comma-separated, left to right): 1,2,3
Enter initial positions (e.g., ABC): ODX
Available reflectors:
  1. I
  2. II
Enter reflector choice (1-2): 1
✓ Code configured successfully!

 5. Process input message
> 5

Enter message to process: HELLO
Output: XYZAB

> 6
✓ Code reset to original positions (O, D, X)
```

## Thread Safety

ConsoleImpl is **NOT thread-safe**. It maintains state and is designed for single-threaded console interaction. Each console instance should be used by one thread.

## Testing

Console includes test helpers:
- `ConsoleImplTester`: Integration testing with engine
- Mock Scanner for automated input testing

## Related Documentation

- [Validation Layer Organization](../docs/VALIDATION_LAYER_ORGANIZATION.md)
- Main [README.md](../README.md)
