# enigma-machine

**Module Purpose**: Pure domain model implementing the mechanical behavior of Enigma machine components.

## Overview

This module contains the core mechanical model of the Enigma machine, implementing the physical components and their behaviors without any UI, validation, or orchestration logic. It is designed to be reusable and testable from other modules.

## Module Layer

**Layer**: Domain/Model Layer  
**Dependencies**: None (only enigma-shared for DTOs)  
**Used By**: enigma-engine

## Responsibilities

### What This Module DOES
- Implements mechanical Enigma components: Rotors, Reflectors, Alphabet, Code, Keyboard
- Executes stepping logic (rightmost rotor always steps, double-stepping on notches)
- Processes signal routing through rotors and reflector
- Generates detailed signal traces for debugging
- Maintains rotor positions during processing

### What This Module DOES NOT DO
- ❌ Perform validation (engine's responsibility)
- ❌ Print or display output (console's responsibility)
- ❌ Parse XML or load specifications (loader's responsibility)
- ❌ Manage history or statistics (engine's responsibility)
- ❌ Handle user input (console's responsibility)

## Constraints and Assumptions

### Preconditions (Assumed by Machine)
The machine assumes all inputs are valid. The following constraints are **not validated** by this module:

**Alphabet:**
- Even length (required for reflector symmetry)
- Unique characters (no duplicates)

**Rotor Configuration:**
- All rotor IDs exist in the specification
- Rotor IDs are unique (no rotor used twice)
- Positions are valid alphabet characters

**Reflector Configuration:**
- Reflector ID exists in the specification
- Reflector mappings are symmetric and complete

**Input Characters:**
- All characters belong to the machine's alphabet
- No forbidden control characters

**Validation Responsibility:** All preconditions are validated by the **engine layer** before delegating to the machine.

### Invariants Maintained by Machine

**Position Consistency:**
- Rotor positions always represent valid alphabet indices
- Position changes are deterministic and reproducible
- Positions are preserved between operations (except during stepping)

**State Determinism:**
- Same initial state + same input → same output (guaranteed)
- No randomness in encryption operations
- Reset operation returns to exact original positions

**Immutability of Code:**
- Once a Code is set on the machine, its rotor/reflector composition is immutable
- Only positions change during processing
- Reconfiguration requires setting a new Code

**Stepping Order:**
- Rightmost rotor (highest index) always steps first
- Stepping propagates leftward based on notch positions
- Stepping completes before signal processing begins

## Key Components

### Machine (`MachineImpl`)
- **Purpose**: Orchestrates the complete encryption process for a single character
- **Process Flow**:
  1. Step rotors (rightmost first, propagate left on notch)
  2. Forward pass: transform signal right→left through rotors
  3. Reflector: symmetric mapping at leftmost position
  4. Backward pass: transform signal left→right through rotors
- **Thread Safety**: Not thread-safe (designed for single-threaded use)

### Rotor (`RotorImpl`)
- **Purpose**: Models a physical Enigma rotor with wiring and stepping
- **Model**: Two-column mechanical model (right column, left column)
- **Stepping**: Rotates both columns by moving top row to bottom
- **Notch**: Signals when next rotor should advance
- **Position**: Current top value of right column (visible in window)

### Reflector (`ReflectorImpl`)
- **Purpose**: Symmetric pairwise mapping at leftmost position
- **Constraint**: If mapping[i] = j, then mapping[j] = i
- **No Self-Mapping**: Typically enforced (though implementation allows it)

### Code (`CodeImpl`)
- **Purpose**: Immutable container for a complete machine configuration
- **Contents**: Alphabet, rotors (left→right), reflector, positions, IDs
- **Immutability**: All collections are defensively copied

### Alphabet
- **Purpose**: Defines the character set for the machine
- **Constraint**: Even length (required for reflector pairs)
- **Conversion**: Char ↔ index mapping

### Keyboard (`KeyboardImpl`)
- **Purpose**: Boundary between characters and indices
- **Responsibility**: Character to index conversion using the alphabet

## Rotor Ordering Convention

**Critical Design Principle**: Rotors are stored in **left→right** order throughout the system, matching the user's visual perspective.

**Indexing:**
- Index 0 = leftmost rotor (visible in left window position)
- Index size-1 = rightmost rotor (steps every character)

**Consistency Across Layers:**
This left→right convention is maintained in:
- `Code.getRotors()` list (domain model)
- `CodeConfig.rotorIds()` list (DTO)
- `CodeConfig.positions()` list (DTO)
- User input and display (console)

**Rationale:** Consistent ordering simplifies reasoning and avoids index-related bugs.

## Signal Processing

### Forward Pass (Right→Left)
Iterates from rightmost rotor to leftmost rotor:
```
for (int i = rotors.size() - 1; i >= 0; i--)
```
Physically moves signal toward the reflector.

### Backward Pass (Left→Right)
Iterates from leftmost rotor to rightmost rotor:
```
for (int i = 0; i < rotors.size(); i++)
```
Physically moves signal back toward the keyboard.

## Stepping Logic

The machine implements the historical Enigma stepping mechanism:

1. **Rightmost rotor always steps** (index size-1): Advances by one position before each character
2. **Notch-triggered propagation**: When a rotor's position equals its notch after stepping, it signals the next rotor to the left to advance
3. **Double-stepping mechanism**: A rotor at its notch position steps both itself and the rotor to its left on the same character (this causes the middle rotor to sometimes advance on consecutive characters)

**Implementation Details:**
- Stepping occurs **before** signal processing for each character
- Stepping begins at the rightmost rotor and propagates leftward
- A rotor "at notch" means: current position equals notch position

**Example Double-Stepping Scenario:**
If the middle rotor is at its notch:
- Character N: Middle rotor steps itself and leftmost rotor
- Character N+1: Rightmost rotor steps, triggers middle rotor (which was already past its notch)

This matches the historical Enigma machine behavior.

## Reset Behavior

**Purpose:** Return rotors to their original positions without clearing configuration or history.

`Machine.reset()` operation:
- Returns rotors to their **original positions** (as configured at setup time)
- **Preserves** the rotor/reflector selection (no component changes)
- Does **NOT** clear history or statistics (engine's responsibility, not machine's)
- Does **NOT** reset the code configuration itself

**Use Case:** Allows re-encryption of messages from the same starting point without reconfiguring the entire machine.

**Important:** This is a mechanical operation that only affects rotor positions. All state management (history, statistics, configuration tracking) is handled by the engine layer.

## Trace Generation

The machine generates detailed `SignalTrace` objects containing:
- Input and output characters
- Rotor window states (before/after)
- Which rotors advanced
- Forward pass transformations (rotor by rotor)
- Reflector transformation
- Backward pass transformations (rotor by rotor)

Traces are used for debugging, testing, and educational display.

## Usage Example

```java
// Create machine
Machine machine = new MachineImpl();

// Configure with a code (created by engine/factory)
machine.setCode(code);

// Process a character
SignalTrace trace = machine.process('A');
System.out.println("Output: " + trace.outputChar());

// Reset to original positions
machine.reset();
```

## Important Notes

### Design Principles

**No Validation:**
- Machine assumes all inputs are pre-validated by the engine
- Invalid inputs may cause undefined behavior or exceptions
- **Rationale:** Keeps domain model pure and focused on mechanics

**Deterministic Behavior:**
- Same input + same initial state → same output (always)
- No randomness in any encryption operation
- Enables exact replay and verification

**Stateful Operation:**
- Rotor positions change with each character processed
- State changes are permanent until explicit reset
- Current state can be captured via `getCodeState()`

**Index-Based Processing:**
- Internal operations use integer indices, not characters
- Keyboard component handles character↔index conversion
- **Rationale:** Simplifies rotor wiring logic and signal transformations

**Left→Right Storage:**
- All rotor lists use left→right indexing (0=leftmost)
- Matches user mental model and display conventions
- Signal processing iterates in physical direction (right→left forward, left→right backward)

### Thread Safety

**Not Thread-Safe:**
- Machine maintains mutable state (rotor positions)
- Designed for single-threaded use within engine
- Concurrent access requires external synchronization

**Recommendation:** For multi-user scenarios (e.g., web server), create separate machine instances per session.

## Related Documentation

- [Rotor Ordering Convention](../docs/ROTOR_ORDERING_CONVENTION.md)
- [Validation Layer Organization](../docs/VALIDATION_LAYER_ORGANIZATION.md)
- Main [README.md](../README.md)
