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

## Constraints Enforced by Engine

### Referential Integrity Constraints
The engine enforces referential integrity between user configurations and the loaded specification:

**Configuration → Specification:**
- Selected rotor IDs must exist in `spec.rotors`
- Selected rotor IDs must be unique (no duplicates)
- Number of rotor IDs must equal `spec.rotorsInUse`
- Selected reflector ID must exist in `spec.reflectors`
- Position characters must exist in `spec.alphabet`
- Plugboard characters must exist in `spec.alphabet`

**Messages → Alphabet:**
- All input characters must exist in `spec.alphabet`
- No forbidden control characters (newline, tab, ESC, ASCII 0-31, 127)

### State Transition Constraints
The engine enforces valid state transitions:

**Loading:**
- A machine specification must be successfully loaded before configuration
- Loading a new specification clears all configuration and history (transactional)
- Invalid XML never modifies the current specification

**Configuration:**
- A specification must be loaded before manual or random configuration
- Configuration records an "original code" for reset and history grouping
- Reconfiguration creates a new original code entry

**Processing:**
- A specification must be loaded before processing
- A code must be configured before processing
- Processing advances rotor positions but never modifies the specification

**Reset:**
- A code must be configured before reset
- Reset returns positions to original values but preserves history

**Terminate:**
- Clears all state but does not exit the application
- Returns engine to uninitialized state

### Semantic Constraints
Beyond referential integrity, the engine enforces semantic rules:

**Plugboard Constraints:**
- Even-length string (character pairs)
- No character appears more than once
- No character maps to itself
- All characters in the machine alphabet

**Uniqueness Constraints:**
- Rotor IDs in configuration must be unique
- Alphabet characters must be unique (enforced at load time by loader)
- Reflector IDs must be unique (enforced at load time by loader)

### Invariants Maintained by Engine

**History Grouping:**
- All messages processed after a configuration are grouped by the original code
- Original code captures rotor IDs and initial positions at configuration time
- History persists through reset but clears on load or terminate

**State Consistency:**
- Engine state is always consistent (either fully initialized or fully uninitialized)
- No partial initialization states exist
- Invalid operations throw exceptions before modifying state

**DTO Immutability:**
- All returned DTOs are immutable snapshots
- Modifications to returned DTOs do not affect engine state
- Engine never exposes internal mutable objects

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
```text
engine.loadMachine("path/to/machine.xml");
```
- Delegates to Loader for XML parsing and validation
- Stores validated MachineSpec
- **Resets history** (new machine = fresh start)
- **Does NOT configure machine** (requires separate config call)
- Transactional: Invalid XML never overwrites existing spec

**XML Schema Location**: Machine XML files should reference the schema located at:
`enigma-loader/src/main/resources/schema/Enigma-Ex2.xsd`

See the enigma-loader README for details on how XML files should reference the schema.

#### 2. Configuration

**Manual Configuration** (`configManual`):
```text
CodeConfig config = new CodeConfig(
    List.of(1, 2, 3),      // rotor IDs (left→right)
    List.of('A', 'B', 'C'), // positions (left→right)
    "I",                    // reflector ID
    ""                      // plugStr (empty for now)
);
engine.configManual(config);
```
- Validates config against loaded spec
- Creates Code via CodeFactory
- Records as new original code in history
- Applies to machine

**Random Configuration** (`configRandom`):
```text
engine.configRandom();
```
- Generates random valid configuration
- Uses SecureRandom for cryptographic quality
- Delegates to configManual for validation and application

#### 3. Processing (`process`)
```text
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
```text
engine.reset();
```
- Returns machine to **original code state** (positions recorded at config time)
- Does **NOT** clear history
- Does **NOT** reset message counter
- Does **NOT** change rotor/reflector selection

#### 5. History (`history`)
```text
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
**Purpose**: Stateless validation helper enforcing semantic constraints.

**Validation Philosophy:**
- **Semantic validation**: Validates meaning relative to the loaded machine specification
- **Relative validation**: Checks existence in spec, not against hardcoded lists
- **Clear error messages**: What's wrong, where it occurred, and how to fix it
- **No duplication**: Each constraint validated exactly once (not in console or loader)

**Validation Categories:**

#### 1. Code Configuration Validation
Validates configuration against the loaded machine specification:

- **Null checks**: Rotor IDs list, positions list, reflector ID must be non-null
- **Count matching**: Number of rotor IDs = number of positions = spec.rotorsInUse
- **Rotor existence**: All selected rotor IDs must exist in spec.rotors
- **Rotor uniqueness**: No rotor ID may appear more than once in configuration
- **Reflector existence**: Selected reflector ID must exist in spec.reflectors
- **Position validity**: All position characters must exist in spec.alphabet
- **Plugboard format**: Even length string (pairs of characters)
- **Plugboard semantics**: No duplicate characters, no self-mapping, all characters in alphabet

**When Validated:** Before creating a Code via CodeFactory (in `configManual()`)

#### 2. Input Message Validation
Validates input messages before processing:

- **Alphabet membership**: Every character must exist in the machine's alphabet
- **Forbidden characters**: Rejects control and non-printable characters:
  - Newline (`\n`, ASCII 10)
  - Tab (`\t`, ASCII 9)
  - Escape (ESC, ASCII 27)
  - All control characters (ASCII 0-31, 127)

**When Validated:** Before processing each message (in `process()`)

**Rationale for Forbidden Characters:** Control characters can cause display issues, break formatting, and are not part of the historical Enigma character set.

#### 3. State Precondition Validation
Validates engine state before operations:

- **Machine loaded**: Spec must be loaded before configuration
- **Machine configured**: Code must be set before processing messages
- **Non-null inputs**: All operation parameters must be non-null

**When Validated:** At the start of each operation that requires preconditions

### MachineHistory
**Purpose**: Records and organizes processing history by original code configuration.

**Data Structure**:
```text
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

The system enforces a three-layer validation architecture to prevent duplication and ensure each constraint is validated at exactly one layer.

### Engine Validates (Semantic - Relative to Spec)
The engine performs **semantic validation** — checking whether inputs make sense in the context of the loaded machine specification:

- **Rotor IDs exist** in loaded spec (not hardcoded lists)
- **Rotor IDs are unique** (no rotor used twice)
- **Reflector ID exists** in loaded spec
- **Position characters** are in the machine's alphabet
- **Input characters** are in the machine's alphabet
- **Plugboard characters** are in the machine's alphabet
- **Plugboard semantics**: No duplicates, no self-mapping
- **State preconditions**: Machine loaded before config, configured before processing

**Why Relative Validation?** The engine validates against the **loaded specification**, not hardcoded values. This allows the engine to work with any valid machine specification without code changes.

**Example:** The engine checks if reflector "II" exists in the currently loaded spec's reflectors list, not whether "II" is a valid Roman numeral (that's the loader's job).

### Engine Does NOT Validate (Format)
The engine **does not** perform format validation. Format checks are the console's responsibility:

- ❌ Command parsing (numeric, in range)
- ❌ Rotor ID list format (comma-separated integers)
- ❌ Position string length matching rotor count (before semantic check)
- ❌ Reflector choice in available range (before ID lookup)
- ❌ Plugboard string even length (format only; semantics validated by engine)

**Rationale:** Format validation is syntactic, not semantic. The console handles string parsing and basic type checking before delegating to the engine.

### Loader Validates (XML Structure)
The loader performs **structural validation** — checking that XML files are well-formed and internally consistent:

- File extension is `.xml`
- Alphabet: even length, unique characters, ASCII only
- Rotor IDs: contiguous sequence 1..N
- Rotor mappings: bijective (complete permutations)
- Notch positions: in alphabet range [1, alphabetSize]
- Reflector IDs: valid Roman numerals (I, II, III, IV, V), contiguous, unique
- Reflector mappings: symmetric, no self-mapping, complete coverage
- Rotors-in-use count: ≤ number of defined rotors

**Rationale:** Structural validation happens at load time and ensures the specification itself is valid. Runtime validation (by engine) then checks user choices against this valid specification.

### Validation Principle: Single Point of Truth
Each constraint is validated at **exactly one layer**:
- **Format** → Console
- **Semantics** → Engine
- **Structure** → Loader

**No duplication.** Each layer trusts that lower layers have done their job, but the engine always validates semantic correctness even if the console skips format checks (defense in depth).

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

```text
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
- Main [README.md](../README.md)
