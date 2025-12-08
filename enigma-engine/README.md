# enigma-engine

**Module Purpose**: Orchestration layer coordinating machine operations, validation, history, and factories.

## Overview

This module provides the public API for interacting with the Enigma machine system. It orchestrates loading specifications, validating configurations, processing messages, and maintaining history. The engine is the integration point between the loader, machine, and console modules.

## Module Layer

**Layer**: Service/Orchestration Layer  
**Dependencies**: enigma-machine, enigma-loader, enigma-shared  
**Used By**: enigma-console, enigma-server (future)

## Responsibilities

### What This Module DOES
- ✅ Load machine specifications via Loader
- ✅ Validate code configurations (rotor IDs, reflector, positions, plugboard)
- ✅ Validate input messages (alphabet membership, forbidden characters)
- ✅ Create Code instances via CodeFactory
- ✅ Coordinate machine processing operations
- ✅ Record and maintain processing history
- ✅ Generate random valid configurations
- ✅ Provide machine state snapshots (DTOs)
- ✅ Reset machine to original configuration

### What This Module DOES NOT DO
- ❌ Parse XML directly (loader's responsibility)
- ❌ Implement mechanical encryption (machine's responsibility)
- ❌ Display output or interact with users (console's responsibility)
- ❌ Format validation (console's responsibility - parsing, A-Z checks, length)

## Key Components

### Engine (`EngineImpl`)
**Purpose**: Main orchestration and coordination class.

**State Management**:
- `spec`: Loaded machine specification (alphabet, rotor specs, reflector specs)
- `ogCodeState`: Original code state (for reset and history grouping)
- `machine`: Internal machine instance
- `history`: Processing history grouped by original code
- `stringsProcessed`: Count of processed messages

**Key Operations**:

#### 1. Loading (`loadMachine`)
```java
engine.loadMachine("path/to/machine.xml");
```
- Delegates to Loader for XML parsing and validation
- Stores validated MachineSpec
- **Resets history** (new machine = fresh start)
- **Does NOT configure machine** (requires separate config call)
- Transactional: Invalid XML never overwrites existing spec

#### 2. Configuration

**Manual Configuration** (`configManual`):
```java
CodeConfig config = new CodeConfig(
    List.of(1, 2, 3),      // rotor IDs (left→right)
    List.of('A', 'B', 'C'), // positions (left→right)
    "I",                    // reflector ID
    ""                      // plugboard (empty for now)
);
engine.configManual(config);
```
- Validates config against loaded spec
- Creates Code via CodeFactory
- Records as new original code in history
- Applies to machine

**Random Configuration** (`configRandom`):
```java
engine.configRandom();
```
- Generates random valid configuration
- Uses SecureRandom for cryptographic quality
- Delegates to configManual for validation and application

#### 3. Processing (`process`)
```java
ProcessTrace trace = engine.process("HELLO");
```
- Validates: machine loaded, machine configured, input not null
- Validates: all input characters in alphabet
- Checks for forbidden characters (newline, tab, ESC, control chars)
- Processes each character through machine
- Collects signal traces
- Records message in history under current original code
- Returns ProcessTrace with output and traces

#### 4. Reset (`reset`)
```java
engine.reset();
```
- Returns machine to **original code state** (positions recorded at config time)
- Does **NOT** clear history
- Does **NOT** reset message counter
- Does **NOT** change rotor/reflector selection

#### 5. History (`history`)
```java
String historyReport = engine.history();
```
- Returns formatted string of all processed messages
- Grouped by original code configuration
- Includes input, output, duration for each message
- Empty if no messages processed

#### 6. Terminate (`terminate`)
**Important**: This method does **NOT** terminate the application.
- Clears internal state (spec, machine config, history)
- Returns engine to uninitialized state
- Does **NOT** call System.exit()
- Name is historical; consider it "clear state"

### EngineValidator
**Purpose**: Stateless validation helper for engine operations.

**Validation Categories**:

1. **Code Configuration Validation**:
   - Null checks (rotor IDs, positions, reflector)
   - Count matching (rotor count = positions count = spec requirement)
   - Rotor IDs exist in spec
   - Rotor IDs are unique
   - Reflector exists in spec (Roman numeral format)
   - Positions are valid alphabet characters
   - Plugboard validation (even length, no duplicates, no self-mapping)

2. **Input Message Validation**:
   - All characters in machine alphabet
   - No forbidden characters:
     - Newline (`\n`)
     - Tab (`\t`)
     - ESC (ASCII 27)
     - All non-printable characters (ASCII 0-31, 127)

**Validation Philosophy**:
- **Semantic validation**: Checks against loaded machine spec
- **Relative validation**: Rotor/reflector IDs checked against spec, not hardcoded
- **Clear error messages**: What's wrong, where, and how to fix

### MachineHistory
**Purpose**: Records and organizes processing history by original code configuration.

**Data Structure**:
```java
Map<CodeState, List<MessageRecord>> history
```

**Key Concepts**:

#### Original Code
The "original code" is the rotor/reflector configuration and initial positions at the moment of configuration. Each time you call `configManual()` or `configRandom()`, a new original code is recorded.

#### Message Grouping
All messages processed after setting a configuration are grouped under that original code, even though rotor positions change with each character. This allows viewing all encryption work done from a particular starting point.

#### When History Resets
History is cleared **only** when:
- A new machine is loaded (`loadMachine`)
- Engine is terminated (`terminate`)

History is **NOT** cleared when:
- Reset is called (`reset`) - positions return to original but history remains
- A new code is configured - new group is added to history

#### History Records
Each `MessageRecord` contains:
- Input string
- Output string
- Processing duration in nanoseconds

### CodeFactory (`CodeFactoryImpl`)
**Purpose**: Constructs Code instances from specifications and configurations.

**Process**:
1. Validate configuration against spec (via EngineValidator)
2. Create Alphabet instance
3. Build Rotors from RotorSpecs:
   - Extract wiring for specified rotor ID
   - Create RotorImpl with wiring and notch
   - Set initial position
4. Build Reflector from ReflectorSpec:
   - Extract mapping for specified reflector ID
   - Create ReflectorImpl
5. Assemble CodeImpl with all components

**Rotor Order**: Maintains left→right order throughout construction.

## Validation Boundaries

### Engine Validates (Semantic):
- Rotor IDs exist in loaded spec
- Rotor IDs are unique
- Reflector ID exists in loaded spec
- Positions are valid alphabet characters
- Input characters are in alphabet
- Plugboard pairs are valid

### Engine Does NOT Validate (Format):
- Command parsing (console responsibility)
- Rotor ID list format (console parses)
- Position string length matching rotor count before parsing (console responsibility)
- Reflector choice in range before ID lookup (console responsibility)

### Loader Validates (XML Structure):
- File extension (.xml)
- Alphabet: even length, unique characters
- Rotor IDs: contiguous 1..N
- Rotor mappings: bijective
- Notch: in alphabet range
- Reflector IDs: Roman numerals I, II, III, IV, V in order
- Reflector mappings: symmetric, no self-mapping, complete coverage

## State Snapshots

The engine provides DTO-based state access:

### `getMachineSpec()`
Returns the currently loaded `MachineSpec` (alphabet, rotor specs, reflector specs, rotors-in-use count).

### `getCurrentCodeConfig()`
Returns the current `CodeConfig` (rotor IDs, positions, reflector ID, plugboard).

### `getTotalProcessedMessages()`
Returns total count of messages processed (increments with each `process()` call).

### `machineData()`
Returns comprehensive `MachineState` including:
- Alphabet
- Available rotor/reflector specs
- Current code configuration
- Current rotor positions
- Total processed message count
- Original code state

## Original vs Current Code State

**Original Code State** (`ogCodeState`):
- Recorded at configuration time
- Contains: rotor IDs, initial positions, reflector ID, plugboard
- Used for reset operations
- Used as key for history grouping

**Current Code State**:
- Retrieved via `machine.getCodeState()`
- Contains: rotor IDs, **current** positions, reflector ID, plugboard
- Changes as characters are processed (positions advance)
- Used for display and state snapshots

The difference is the **positions**: original has initial positions, current has post-processing positions.

## Usage Example

```java
// Create engine
Engine engine = new EngineImpl();

// Load machine specification
engine.loadMachine("enigma.xml");

// Configure manually
CodeConfig config = new CodeConfig(
    List.of(1, 2, 3),
    List.of('O', 'D', 'X'),
    "I",
    ""
);
engine.configManual(config);

// Process message
ProcessTrace trace = engine.process("HELLO");
System.out.println("Output: " + trace.output());

// Reset to original positions
engine.reset();

// View history
System.out.println(engine.history());

// Generate random config
engine.configRandom();

// Get current state
MachineSpec spec = engine.getMachineSpec();
CodeConfig currentConfig = engine.getCurrentCodeConfig();
long messageCount = engine.getTotalProcessedMessages();
```

## Thread Safety

The engine is **NOT thread-safe**. It maintains mutable state and is designed for single-threaded use (console application). For concurrent use (web server), create one engine instance per session/user.

## Exception Hierarchy

- `EngineException`: Base exception for engine errors
  - `MachineNotLoadedException`: No spec loaded
  - `MachineNotConfiguredException`: No code configured
  - `InvalidConfigurationException`: Invalid code configuration
  - `InvalidMessageException`: Invalid input message

All exceptions include descriptive error messages with fix suggestions.

## Related Documentation

- [Validation Layer Organization](../docs/VALIDATION_LAYER_ORGANIZATION.md)
- [Integration Dev UI](../docs/integration-dev-ui.md)
- Main [README.md](../README.md)
