# enigma-shared

**Module Purpose**: Shared DTOs, specifications, and common types used across all modules.

## Overview

This module contains Data Transfer Objects (DTOs), specification records, state snapshots, and other shared types used for communication between modules. It has no dependencies on other enigma modules and serves as a common vocabulary for the system.

## Module Layer

**Layer**: Shared/Common Layer  
**Dependencies**: None (Java standard library only)  
**Used By**: All modules (machine, engine, loader, console)

## Purpose

The shared module keeps module coupling low by centralizing cross-cutting types. Instead of modules depending on each other's implementation classes, they communicate through immutable DTOs and records defined here.

## Key Packages

### `enigma.shared.spec`
Immutable specification records describing machine components loaded from XML.

### `enigma.shared.dto`
Data Transfer Objects for configuration, tracing, and records.

### `enigma.shared.state`
State snapshot records capturing machine/code state at a point in time.

## Specifications (`spec` package)

### MachineSpec
**Purpose**: Complete machine specification loaded from XML.

```java
public record MachineSpec(
    String alphabet,                  // e.g., "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    int rotorsInUse,                  // number of rotors in configuration (e.g., 3)
    List<RotorSpec> rotors,           // available rotor specifications
    List<ReflectorSpec> reflectors    // available reflector specifications
)
```

**Usage**:
- Created by: Loader (from XML)
- Used by: Engine (validation), Console (display options)
- Immutable: Thread-safe, can be shared

### RotorSpec
**Purpose**: Specification for a single rotor.

```java
public record RotorSpec(
    int id,                           // rotor identifier (1, 2, 3, ...)
    char[] rightColumn,               // right-side wiring (top→bottom)
    char[] leftColumn,                // left-side wiring (top→bottom)
    int notch                         // notch index (0-based)
)
```

**Wiring Model**:
- `rightColumn`: Keyboard-facing contacts (entry side)
- `leftColumn`: Reflector-facing contacts (exit side)
- Both arrays are in **top→bottom** order as defined in XML
- Arrays are mutable (for efficiency), but callers should treat as immutable

**Notch**:
- 0-based index into the alphabet
- When rotor position equals notch after stepping, next rotor advances

### ReflectorSpec
**Purpose**: Specification for a reflector.

```java
public record ReflectorSpec(
    String id,                        // reflector identifier ("I", "II", "III", ...)
    int[] mapping                     // symmetric mapping array
)
```

**Mapping Model**:
- `mapping[i] = j` means index i maps to index j
- Symmetric: `mapping[i] = j` implies `mapping[j] = i`
- Complete: All indices [0, alphabetSize-1] are covered
- Array is mutable (for efficiency), but callers should treat as immutable

## Configuration DTOs (`dto.config` package)

### CodeConfig
**Purpose**: User-provided code configuration.

```java
public record CodeConfig(
    List<Integer> rotorIds,           // selected rotor IDs (left→right)
    List<Character> positions,        // initial positions (left→right)
    String reflectorId,               // selected reflector ID
    String plugboard                  // plugboard pairs (optional, e.g., "ABCD" = A↔B, C↔D)
)
```

**Rotor Ordering**:
- **Left→right**: Index 0 = leftmost rotor, size-1 = rightmost rotor
- User input "1,2,3" with positions "ABC" means:
  - Leftmost rotor: ID 1, position 'A'
  - Middle rotor: ID 2, position 'B'
  - Rightmost rotor: ID 3, position 'C'

**Positions**:
- Characters from the machine alphabet (e.g., 'A', 'B', 'C', ...)
- Represent visible window positions
- Change during processing (rightmost advances every character)

**Plugboard** (Exercise 2):
- Even-length string of character pairs
- Example: "ABCD" means A↔B, C↔D
- Empty string "" means no plugboard

**Usage**:
- Created by: Console (user input), Engine (random generation)
- Used by: Engine (validation), CodeFactory (code construction)
- Immutable: Safe to pass around

## State Snapshots (`state` package)

### CodeState
**Purpose**: Snapshot of code configuration and current positions.

```java
public record CodeState(
    List<Integer> rotorIds,           // rotor IDs (left→right)
    List<Character> positions,        // current positions (left→right)
    List<Integer> notchDistances,     // distance to next notch for each rotor
    String reflectorId,               // reflector ID
    String plugboard                  // plugboard pairs
)
```

**Positions**:
- **Current** positions (change during processing)
- Contrast with `CodeConfig.positions` which are **initial** positions

**Notch Distances**:
- For each rotor: how many steps until notch is reached
- Useful for predicting when next rotor will advance
- Example: [5, 12, 3] means leftmost rotor is 5 steps from notch

**Usage**:
- Created by: Machine (snapshot of current state)
- Used by: Engine (history key, display), Console (display)
- Represents state at a specific moment in time

**Original vs Current**:
- **Original CodeState**: Captured at configuration time (engine stores as `ogCodeState`)
- **Current CodeState**: Captured from machine at any time (positions may differ)
- Original is used as **history key** and **reset target**

### MachineState
**Purpose**: Comprehensive machine state snapshot.

```java
public record MachineState(
    String alphabet,                  // machine alphabet
    int rotorsInUse,                  // rotors in configuration
    List<RotorSpec> availableRotors,  // all available rotors
    List<ReflectorSpec> availableReflectors, // all available reflectors
    CodeState currentCode,            // current code state (may be null)
    CodeState originalCode,           // original code state (may be null)
    long totalProcessedMessages       // count of processed messages
)
```

**Usage**:
- Created by: Engine (`machineData()` method)
- Used by: Console (comprehensive display)
- Combines spec, current state, and statistics

## Trace DTOs (`dto.tracer` package)

### SignalTrace
**Purpose**: Complete trace of one character's encryption path.

```java
public record SignalTrace(
    char inputChar,                   // original input character
    char outputChar,                  // final output character
    String windowBefore,              // rotor window before stepping (e.g., "ODX")
    String windowAfter,               // rotor window after processing (e.g., "ODY")
    List<Integer> advancedIndices,    // which rotors stepped (0=leftmost)
    List<RotorTrace> forwardSteps,    // rotor transformations right→left
    ReflectorTrace reflectorStep,     // reflector transformation
    List<RotorTrace> backwardSteps    // rotor transformations left→right
)
```

**Window Strings**:
- Left→right character positions (e.g., "ODX" = O at leftmost, X at rightmost)
- `windowBefore`: Positions before stepping
- `windowAfter`: Positions after stepping and processing

**Advanced Indices**:
- List of rotor indices that stepped (0-based)
- Example: [2] means only rightmost rotor stepped
- Example: [1, 2] means middle and rightmost stepped (double-stepping)

**Usage**:
- Created by: Machine (`process()` method)
- Used by: Console (detailed trace display), Testing (verification)

### ProcessTrace
**Purpose**: Bundle of output string and per-character traces.

```java
public record ProcessTrace(
    String output,                    // final encrypted/decrypted string
    List<SignalTrace> signalTraces    // one trace per input character
)
```

**Usage**:
- Created by: Engine (`process()` method)
- Used by: Console (display output and traces)

### RotorTrace
**Purpose**: Single rotor's transformation during signal processing.

```java
public record RotorTrace(
    int rotorIndex,                   // rotor position (0=leftmost)
    int id,                           // rotor ID from spec
    int entryIndex,                   // input index
    int exitIndex                     // output index
)
```

**Usage**:
- Created by: Machine (during forward/backward passes)
- Used by: SignalTrace (bundled into forward/backward steps)

### ReflectorTrace
**Purpose**: Reflector's transformation during signal processing.

```java
public record ReflectorTrace(
    String id,                        // reflector ID (e.g., "I")
    int entryIndex,                   // input index
    int exitIndex                     // output index (paired index)
)
```

**Usage**:
- Created by: Machine (during reflector step)
- Used by: SignalTrace (bundled as reflector step)

## Record DTOs (`dto.record` package)

### MessageRecord
**Purpose**: Record of a processed message in history.

```java
public record MessageRecord(
    String input,                     // input message
    String output,                    // output message
    long durationNanos                // processing duration in nanoseconds
)
```

**Usage**:
- Created by: Engine (during `process()`)
- Stored by: MachineHistory (grouped by original code)
- Displayed by: Console (history command)

## Design Principles

### Immutability
All DTOs are **immutable records** (except arrays in specs for efficiency). This ensures:
- Thread-safety
- No defensive copying needed when passing between modules
- Clear ownership semantics

### No Business Logic
DTOs contain **no business logic**, only:
- Data fields
- Optional `toString()` for debugging/display
- No validation, transformation, or computation

### Module Independence
Shared module has **no dependencies** on other enigma modules. This prevents circular dependencies and keeps module boundaries clean.

### Semantic Naming
DTOs follow consistent naming conventions to indicate their purpose:

- **Spec**: Immutable specification loaded from XML
  - `MachineSpec`, `RotorSpec`, `ReflectorSpec`
  - Represents the machine definition, not runtime state
  
- **Config**: User-provided configuration
  - `CodeConfig`
  - Represents user choices for machine setup
  
- **State**: Runtime state snapshot
  - `CodeState`, `MachineState`
  - Captures current or original machine state at a point in time
  
- **Trace**: Detailed processing trace
  - `SignalTrace`, `ProcessTrace`, `RotorTrace`, `ReflectorTrace`
  - Records the transformation path of characters through the machine
  
- **Record**: Historical record
  - `MessageRecord`
  - Preserves information about past operations

### Ordering Convention — Left→Right Throughout

**Critical Design Principle:** All rotor-related lists use **left→right ordering** consistently across all DTOs.

**Rotor Indexing:**
- Index 0 = leftmost rotor (visible in left window position)
- Index size-1 = rightmost rotor (steps every character)

**Applied In:**
- `CodeConfig.rotorIds`: [1,2,3] means leftmost=1, middle=2, rightmost=3
- `CodeConfig.positions`: ['A','B','C'] means leftmost='A', middle='B', rightmost='C'
- `CodeState.rotorIds`: Same ordering as CodeConfig
- `CodeState.positions`: Same ordering as CodeConfig
- `SignalTrace.windowBefore/After`: "ABC" means leftmost='A', middle='B', rightmost='C'

**Consistency Benefits:**
- Matches user's visual perspective of the machine
- Aligns with physical left-to-right window arrangement
- Simplifies console input and display logic
- Eliminates index confusion and reversal bugs
- Consistent across all layers: console → engine → machine

**Example:**
User configures: "Rotors 3,2,1 at positions ODX"
- DTO storage: `rotorIds=[3,2,1]`, `positions=['O','D','X']`
- Machine interpretation: leftmost=3@O, middle=2@D, rightmost=1@X
- Display output: "ODX" (left to right)

## DTO Design Constraints and Guarantees

### Immutability Guarantees

**Record Immutability:**
- All DTO classes are Java records (immutable by default)
- Fields are final and cannot be reassigned
- No setter methods exist
- Defensive copies in constructors where needed

**Array Exception:**
- `RotorSpec.rightColumn`, `RotorSpec.leftColumn` (char arrays)
- `ReflectorSpec.mapping` (int array)
- **Warning:** Arrays are mutable for efficiency; callers must treat as immutable
- **Future consideration:** May use immutable collections in later versions

**Thread-Safety:**
- All DTOs are thread-safe (immutable state)
- Can be safely shared between threads without synchronization
- No concurrent modification issues
- Safe for use in multi-threaded server environments

### No Business Logic Constraint

DTOs are **pure data containers** with zero business logic:

**Allowed:**
- Data fields (final, immutable)
- Record-generated methods (`equals()`, `hashCode()`, `toString()`)
- Optional custom `toString()` for debugging/display formatting

**Forbidden:**
- Validation logic (delegated to validators in engine/loader layers)
- Transformation logic (delegated to factories and processors)
- Computation logic (delegated to business logic layers)
- State mutation (immutability is a hard constraint)

**Rationale:** Keeping DTOs logic-free maintains clean separation between data and behavior, simplifies testing, and prevents coupling between layers.

### Module Independence

**Zero Dependencies on Other Enigma Modules:**
- Shared module depends only on Java standard library
- Never imports from enigma-machine, enigma-engine, enigma-loader, or enigma-console
- Prevents circular dependencies
- Allows any module to depend on shared without coupling issues

**Benefits:**
- Clear module boundaries
- Simplified dependency graph
- Easier testing and maintenance
- Modules communicate only through shared contracts

## Thread Safety

All DTOs are **immutable** (except arrays which should be treated as immutable). They are thread-safe and can be shared between threads without synchronization.

**Exception**: `RotorSpec` and `ReflectorSpec` contain mutable arrays (`char[]`, `int[]`). Callers should not modify these arrays. Future versions may use immutable collections.

## Testing

DTOs are simple data containers with no logic to test. Testing focuses on:
- Correct construction
- Immutability (cannot modify after creation)
- Proper `equals()` and `hashCode()` (provided by record)

## Related Documentation

- [Rotor Ordering Convention](../docs/ROTOR_ORDERING_CONVENTION.md)
- Main [README.md](../README.md)
