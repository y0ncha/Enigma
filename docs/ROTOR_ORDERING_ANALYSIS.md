# Rotor Ordering Consistency Analysis

## Issue Requirements

The issue asks to verify:
1. Input rotor order is left→right (user perspective)
2. Internal rotor order is right→left (machine perspective) ❌ **CLARIFICATION NEEDED**
3. Position strings match this conversion
4. All stepping logic relies on rightmost rotor being index 0 (or equivalent consistent mapping)

## Actual Implementation Analysis

### 1. Input Rotor Order (User Perspective)

✅ **VERIFIED**: Input is left→right

**Evidence:**
- `CodeConfig` documentation (line 13-17): "rotorIds: [1, 2, 3] means rotor 1 leftmost, rotor 3 rightmost"
- `InputParsers.buildInitialPositions()`: Preserves input order without reversal
- Console code creates `CodeConfig(rotorIds, initialPositions, reflectorId)` directly

### 2. Internal Rotor Storage

✅ **VERIFIED**: Internal storage is left→right (NOT right→left as issue states)

**Evidence:**
- `CodeImpl` (line 26): `private final List<Rotor> rotors; // left → right (index 0 = leftmost)`
- `CodeFactoryImpl.buildRotors()`: Builds rotors in left→right order, preserving input order
- `MachineImpl` (line 38): "Rotors passed in Code#getRotors() are expected left→right (index 0 = leftmost)"

**Important Finding:** The issue statement appears to contain an error. It says "Internal rotor order is right→left (machine perspective)" but the actual implementation stores rotors left→right.

### 3. Position String Matching

✅ **VERIFIED**: Position strings match left→right order

**Evidence:**
- `CodeConfig` (line 40): `positions` are "starting positions as characters (left→right)"
- `buildInitialPositions()`: Returns positions in same order as input
- `CodeFactoryImpl.buildRotors()`: Matches `positions[i]` to `rotorIds[i]` directly

**Example:**
```
Input: <3,2,1><ABC>
Internal: rotors[0] = Rotor 3 at 'A' (leftmost)
          rotors[1] = Rotor 2 at 'B' (middle)
          rotors[2] = Rotor 1 at 'C' (rightmost)
```

### 4. Stepping Logic and Rightmost Rotor

✅ **VERIFIED**: Rightmost rotor steps first, using highest index

**Evidence:**
- `MachineImpl.advance()` (line 210): `int index = rotors.size() - 1; // start at RIGHTMOST (last index)`
- Stepping starts at highest index and propagates leftward
- For 3 rotors: rightmost is index 2, leftmost is index 0

**Clarification on "index 0" in issue:**
The issue says "rightmost rotor being index 0 (or equivalent consistent mapping)". This appears to be ambiguous. In our implementation:
- Rightmost rotor is at index `size-1` (NOT index 0)
- This is consistent because we use left→right indexing throughout

## Consistency Verification

### Test Case: Configuration `<3,2,1><ABC><I>`

**Expected Mapping:**
- User input: Rotor IDs [3, 2, 1] left→right, Positions ['A', 'B', 'C'] left→right
- Internal storage:
  - `rotors[0]` = Rotor 3 at position 'A' (leftmost)
  - `rotors[1]` = Rotor 2 at position 'B' (middle)
  - `rotors[2]` = Rotor 1 at position 'C' (rightmost)

**Verification Steps:**
1. ✅ CodeConfig stores [3,2,1] and ['A','B','C'] without reversal
2. ✅ CodeFactory builds rotors at indices [0,1,2] as [Rotor3(A), Rotor2(B), Rotor1(C)]
3. ✅ Machine stores rotors in same order
4. ✅ Stepping starts at index 2 (rightmost, Rotor 1)
5. ✅ Forward pass iterates 2→1→0 (right→left physically)
6. ✅ Backward pass iterates 0→1→2 (left→right physically)

### Consistency Statement

**The implementation is internally consistent:**

1. **Storage Convention**: Left→right throughout (CodeConfig → Code → Machine)
2. **Index Convention**: 0 = leftmost, size-1 = rightmost
3. **Stepping Convention**: Always starts at index size-1 (rightmost)
4. **Processing Convention**: 
   - Forward uses high→low indices (physical right→left)
   - Backward uses low→high indices (physical left→right)

## Issue Statement Discrepancy

The issue states: "Internal rotor order is right→left (machine perspective)"

This statement appears to be **incorrect or misleading**. The actual implementation:
- Stores rotors left→right in arrays
- Processes signals in physical right→left direction (forward) and left→right direction (backward)
- Uses left→right indexing (0=left, size-1=right) consistently

**Possible Interpretations:**
1. The issue author may have confused "signal flow direction" with "storage order"
2. The term "machine perspective" may refer to signal processing order, not storage order
3. There may be a different design intention that wasn't implemented

## Recommendation

The current implementation is **correct and consistent**:
- Uses left→right storage convention throughout
- Correctly implements physical signal flow (right→left forward, left→right backward)
- Stepping logic correctly identifies rightmost rotor as highest index

**No code changes are needed.** However, documentation has been added to:
1. Clarify the left→right convention
2. Explain the relationship between storage order and signal flow
3. Provide test cases to verify consistency

## Documentation Added

1. `docs/ROTOR_ORDERING_CONVENTION.md` - Comprehensive explanation
2. `RotorOrderingConsistencyTester.java` - Test to verify conventions
3. Enhanced comments in:
   - `CodeFactoryImpl.buildRotors()`
   - `MachineImpl.advance()`
   - `MachineImpl.forwardTransform()`
   - `MachineImpl.backwardTransform()`
4. Fixed `.copilot.md` incorrect statement
