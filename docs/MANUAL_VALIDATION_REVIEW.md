# Manual Code Configuration Validation Summary

## Overview
This document summarizes the manual configuration validation enhancements made to `EngineValidator.java` and `EngineImpl.java`.

## Validation Scope and Responsibilities

**Important:** All rotor ID and reflector ID validations in the Engine layer are **relative to the MachineSpec**. The Engine validator checks if IDs exist in the loaded machine specification, not against hardcoded lists.

- **XML Loader responsibility:** Validate that reflector IDs are valid Roman numerals (I, II, III, IV, V) during XML parsing
- **Engine validator responsibility:** Check if the provided rotor/reflector IDs exist in the loaded MachineSpec

This separation ensures:
1. XML structural validation happens once at load time
2. Engine runtime validation focuses on user configuration against the loaded spec
3. No duplication of validation logic across layers

## Changes Made

### 1. Reflector ID Validation (Relative to MachineSpec)

**Location:** `EngineValidator.validateReflectorExists()`

**Enhancement:** Validates that the reflector ID exists in the MachineSpec. The Roman numeral format validation is handled by the XML Loader during machine specification loading.

**Implementation:**
```java
public static void validateReflectorExists(MachineSpec spec, String reflectorId) {
    if (reflectorId.isBlank()) throw new IllegalArgumentException("reflectorId must be non-empty");
    if (spec.getReflectorById(reflectorId) == null)
        throw new IllegalArgumentException("Reflector '" + reflectorId + "' does not exist");
}
```

**Test Cases:**
- Reflector ID not in spec (e.g., "III" when spec only has I, II) → throws exception
- Invalid/malformed ID not in spec (e.g., "INVALID") → throws exception  
- Valid reflector ID that exists in spec → passes validation

**Note:** If an invalid Roman numeral makes it to this validation, it will fail the existence check since the XML loader would have rejected it during machine loading.

### 2. Plugboard Validation (Prepared for EX2)

**Location:** `EngineValidator.validatePlugboard()`

**Enhancement:** Added comprehensive plugboard validation method to prepare for Exercise 2, even though plugboard is not yet in CodeConfig.

**Validation Rules:**
1. Even-length string (pairs of characters)
2. No character appears more than once
3. No self-mapping (e.g., "AA")
4. All characters must be in the machine alphabet

**Implementation:**
```java
public static void validatePlugboard(MachineSpec spec, String plugboard) {
    // null or empty plugboard is valid (no plugboard configured)
    if (plugboard == null || plugboard.isEmpty()) {
        return;
    }
    
    // Check even length
    if (plugboard.length() % 2 != 0) {
        throw new IllegalArgumentException(
            "Plugboard configuration must have even length (pairs of characters), got length " + plugboard.length());
    }
    
    Set<Character> seenChars = new HashSet<>();
    
    // Process pairs
    for (int i = 0; i < plugboard.length(); i += 2) {
        char first = plugboard.charAt(i);
        char second = plugboard.charAt(i + 1);
        
        // Check for self-mapping
        if (first == second) {
            throw new IllegalArgumentException(
                "Plugboard cannot map a letter to itself: '" + first + first + "'");
        }
        
        // Check for duplicate characters
        if (!seenChars.add(first)) {
            throw new IllegalArgumentException(
                "Plugboard letter '" + first + "' appears more than once");
        }
        if (!seenChars.add(second)) {
            throw new IllegalArgumentException(
                "Plugboard letter '" + second + "' appears more than once");
        }
        
        // Check characters are in alphabet
        if (!spec.alphabet().contains(first)) {
            throw new IllegalArgumentException(
                "Plugboard character '" + first + "' is not in the machine alphabet");
        }
        if (!spec.alphabet().contains(second)) {
            throw new IllegalArgumentException(
                "Plugboard character '" + second + "' is not in the machine alphabet");
        }
    }
}
```

**Test Cases:**
- Odd length like "ABC" → throws exception
- Character appears twice like "ABAC" → throws exception
- Self-mapping like "AA" → throws exception
- Character not in alphabet like "AZ" (when alphabet is "ABCD") → throws exception
- Valid plugboard like "AB" → passes validation
- Null or empty plugboard → passes validation (no plugboard configured)

### 3. Interface Updates

**Location:** `Engine.java` and `EngineImpl.java`

**Enhancement:** Added `validatePlugboard()` method to the Engine interface and implementation.

```java
// Engine.java
void validatePlugboard(MachineSpec spec, String plugboard);

// EngineImpl.java
@Override
public void validatePlugboard(MachineSpec spec, String plugboard) {
    EngineValidator.validatePlugboard(spec, plugboard);
}
```

## Existing Validations (Already Implemented)

The following validations were already present and are working correctly:

1. **Rotor Count Validation** - Ensures exactly 3 rotors are selected (ROTORS_IN_USE constant)
2. **Rotor ID Existence** - Validates all rotor IDs exist in the machine spec (relative to loaded spec)
3. **Rotor ID Uniqueness** - Ensures no duplicate rotor IDs
4. **Position Count** - Validates position string length matches rotor count
5. **Position Alphabet Membership** - Ensures all position characters are in the alphabet
6. **Reflector Existence** - Validates reflector exists in the machine spec (relative to loaded spec)

## Complete Validation Flow

When `EngineValidator.validateCodeConfig()` is called, it performs validation in this order:

1. Null checks (rotorIds, positions, reflectorId)
2. Rotor and position counts (must be exactly 3)
3. Rotor IDs existence and uniqueness (checks against MachineSpec)
4. Reflector exists (checks against MachineSpec)
5. Positions are in alphabet

The plugboard validation is a separate method that can be called independently when needed.

## Test Coverage

A comprehensive test suite has been created at:
`enigma-engine/src/test/enigma/engine/validation/EngineValidatorTester.java`

This tester includes 15 test cases covering:
- Wrong rotor counts (2 and 4 rotors)
- Duplicate rotor IDs
- Non-existent rotor IDs (relative to mock MachineSpec)
- Wrong position string length
- Position characters not in alphabet
- Reflector IDs that don't exist in the spec
- All plugboard validation scenarios
- Valid configurations

## State Safety

All validation failures throw `IllegalArgumentException` with clear error messages. **No machine state is mutated on validation failure**, maintaining the critical invariant that invalid configurations never override the existing machine state.

## Forward Compatibility

The plugboard validation is designed to be forward-compatible with Exercise 2:
- Handles null/empty plugboard gracefully (no plugboard configured)
- Ready to be integrated when CodeConfig is extended with a plugboard field
- All validation logic is centralized and reusable

## Error Messages

All error messages follow a consistent pattern:
- Clear identification of the problem
- Specific values that caused the failure
- Expected format or range (when applicable)

Examples:
- "Exactly 3 rotors must be selected"
- "Duplicate rotor 2"
- "Rotor 99 does not exist in spec"
- "Reflector 'III' does not exist" (when spec only has I, II)
- "Plugboard cannot map a letter to itself: 'AA'"
- "Plugboard letter 'A' appears more than once"
