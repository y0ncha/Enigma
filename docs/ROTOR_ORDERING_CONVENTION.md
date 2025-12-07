# Rotor Ordering Convention

## Overview

This document clarifies the rotor ordering conventions used throughout the Enigma machine implementation. Understanding these conventions is critical for correct machine configuration and operation.

## Key Principle: Left→Right Throughout

**All data structures use left→right ordering**, matching the user-facing machine window view.

### Visual Representation

```
User View (Physical Machine Window):
┌───────────────────────────────┐
│  Leftmost   Middle  Rightmost │
│     A         B        C       │
│  Rotor 3   Rotor 2  Rotor 1   │
└───────────────────────────────┘
     ↑         ↑         ↑
   Index 0   Index 1   Index 2
```

## Data Structures

### 1. CodeConfig (DTO)

```java
CodeConfig config = new CodeConfig(
    List.of(3, 2, 1),         // rotorIds: left→right
    List.of('A', 'B', 'C'),   // positions: left→right
    "I"                        // reflector
);
```

**Convention:**
- `rotorIds[0]` = leftmost rotor ID (Rotor 3 in example)
- `rotorIds[1]` = middle rotor ID (Rotor 2 in example)
- `rotorIds[2]` = rightmost rotor ID (Rotor 1 in example)
- Same indexing applies to `positions`

### 2. Code/Machine Internal Storage

The `Code` and `Machine` classes store rotors in the **same left→right order** as `CodeConfig`:

```java
List<Rotor> rotors = code.getRotors();
// rotors[0] = leftmost rotor (Rotor 3 in example)
// rotors[1] = middle rotor (Rotor 2 in example)
// rotors[2] = rightmost rotor (Rotor 1 in example)
```

**Important:** There is **no reversal** between user input and internal storage. The order is preserved throughout.

## Signal Processing Direction

While storage is left→right, signal processing follows the physical Enigma path:

### Forward Pass (Keyboard → Reflector)

Signal travels **right→left** through rotors:
1. Enter rightmost rotor (index 2)
2. Through middle rotor (index 1)
3. Through leftmost rotor (index 0)
4. Reach reflector

```java
// MachineImpl.forwardTransform()
for (int i = rotors.size() - 1; i >= 0; i--) {
    // Process from rightmost to leftmost
}
```

### Backward Pass (Reflector → Keyboard)

Signal travels **left→right** through rotors:
1. Exit reflector
2. Through leftmost rotor (index 0)
3. Through middle rotor (index 1)
4. Through rightmost rotor (index 2)
5. Return to keyboard

```java
// MachineImpl.backwardTransform()
for (int i = 0; i < rotors.size(); i++) {
    // Process from leftmost to rightmost
}
```

## Stepping Logic

The rightmost rotor **always steps first** before each character encryption:

```java
// MachineImpl.advance()
int index = rotors.size() - 1;  // Start at rightmost (last index)
do {
    Rotor rotor = rotors.get(index);
    shouldAdvance = rotor.advance();
    advanced.add(index);
    index--;  // Move leftward
} while (shouldAdvance && index >= 0);
```

**Key points:**
- Rightmost rotor = highest index in the array
- Stepping propagates leftward when a notch is reached
- All stepping logic uses the same left→right indexing

## Example Mappings

### Configuration: `<3,2,1><ABC><I>`

| User View | Internal Representation | Description |
|-----------|------------------------|-------------|
| Leftmost position 'A' | `rotors[0]` with ID 3, position 'A' | First in array |
| Middle position 'B' | `rotors[1]` with ID 2, position 'B' | Second in array |
| Rightmost position 'C' | `rotors[2]` with ID 1, position 'C' | Third in array |

### Configuration: `<1,2,3><ODX><I>`

| User View | Internal Representation | Description |
|-----------|------------------------|-------------|
| Leftmost position 'O' | `rotors[0]` with ID 1, position 'O' | First in array |
| Middle position 'D' | `rotors[1]` with ID 2, position 'D' | Second in array |
| Rightmost position 'X' | `rotors[2]` with ID 3, position 'X' | Third in array |

## Common Misunderstandings

### ❌ Myth: "Rotors are stored right→left internally"

**False.** Rotors are stored left→right in all data structures. The confusion arises because:
- Signal processing uses right→left iteration (forward pass)
- But the underlying array is still indexed left→right

### ❌ Myth: "Position string must be reversed"

**False.** Position strings match the array ordering directly:
- Input `"ABC"` → stored as `['A', 'B', 'C']`
- `positions[0] = 'A'` applies to `rotors[0]` (leftmost)
- No reversal needed

### ✓ Correct: "Rightmost rotor has the highest index"

**True.** In a 3-rotor configuration:
- Leftmost rotor = index 0
- Rightmost rotor = index 2
- Stepping starts at index 2

## Verification Test

The `RotorOrderingConsistencyTester` class verifies:

1. ✓ Configuration `<3,2,1><ABC><I>` creates:
   - `rotors[0]` = Rotor 3 at 'A'
   - `rotors[1]` = Rotor 2 at 'B'
   - `rotors[2]` = Rotor 1 at 'C'

2. ✓ Stepping advances the rightmost rotor (index 2) first

3. ✓ Position strings maintain left→right ordering

4. ✓ Known test vectors produce correct output

## Summary

- **Storage:** Left→right throughout (CodeConfig, Code, Machine)
- **Indexing:** `0 = leftmost`, `size-1 = rightmost`
- **Processing:** Right→left forward, left→right backward
- **Stepping:** Starts at rightmost (highest index)
- **No reversal** between user input and internal representation

This consistent convention simplifies reasoning about the system and avoids common bugs related to array indexing and position mapping.
