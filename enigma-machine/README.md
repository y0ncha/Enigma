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

**Critical**: Rotors are stored in **left→right** order throughout the system.
- Index 0 = leftmost rotor (visible in window position 1)
- Index size-1 = rightmost rotor (steps every time)

This convention is consistent across:
- `Code.getRotors()` list
- `CodeConfig.rotorIds()` list
- `CodeConfig.positions()` list
- User input and display

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

1. **Always step rightmost rotor** (index size-1)
2. **Check for notch**: If rotor position equals notch after stepping
3. **Propagate left**: Step next rotor to the left if notch triggered
4. **Double-stepping**: A rotor at its notch steps itself and propagates

The stepping happens **before** signal processing for each character.

## Reset Behavior

`Machine.reset()`:
- Returns rotors to their **original positions** (as configured)
- Does **NOT** clear history or statistics (engine's responsibility)
- Does **NOT** reset the code configuration (maintains rotors/reflector)

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

1. **No Validation**: Machine assumes all inputs are valid (validated by engine)
2. **Deterministic**: Same input + same initial state → same output
3. **Stateful**: Rotor positions change with each character processed
4. **Index-Based Internally**: Uses integer indices; keyboard handles char conversion
5. **Left→Right Storage**: All rotor lists use left→right indexing

## Related Documentation

- [Rotor Ordering Convention](../docs/ROTOR_ORDERING_CONVENTION.md)
- [Validation Layer Organization](../docs/VALIDATION_LAYER_ORGANIZATION.md)
- Main [README.md](../README.md)
