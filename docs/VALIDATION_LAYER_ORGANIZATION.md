# Validation Layer Organization

**Status:** ✅ Complete and Compliant  
**Last Updated:** December 7, 2024  
**Related Issues:** #86, #85, #84

## Overview

This document describes the final validation layer organization in the Enigma machine project. The validation is cleanly separated across three layers: **Console**, **Engine**, and **Loader**.

## Architectural Principles

### 1. Console Contains ONLY Format Checks

**Definition:** Format validation checks the syntactic structure of input without understanding its semantic meaning.

**Console Validation (ConsoleValidator.java):**
- ✅ `parseCommand()` - validates command is numeric and in range [1-8]
- ✅ `ensurePositionsLengthMatches()` - validates positions string length matches rotor count
- ✅ `ensureReflectorChoiceInRange()` - validates reflector choice is in range [1-N]
- ✅ `validatePlugboardFormat()` - validates plugboard has even length

**What Console Does NOT Validate:**
- ❌ Alphabet membership (e.g., whether 'Z' is in the alphabet)
- ❌ Rotor/reflector existence in machine spec
- ❌ Rotor uniqueness
- ❌ Any semantic rules about the machine

### 2. Engine Contains ALL Semantic Validation

**Definition:** Semantic validation checks whether the input makes sense in the context of the machine specification.

**Engine Validation (EngineValidator.java):**
- ✅ `validateCodeConfig()` - orchestrator for all configuration validation
  - `validateNullChecks()` - null checks for rotor IDs, positions, reflector
  - `validateRotorAndPositionCounts()` - counts match ROTORS_IN_USE from spec
  - `validateRotorIdsExistenceAndUniqueness()` - rotors exist in spec and are unique
  - `validateReflectorExists()` - reflector exists in spec
  - `validatePositionsInAlphabet()` - positions are in machine alphabet
  - `validatePlugboard()` - plugboard semantic rules (alphabet membership, no duplicates, no self-mapping)
- ✅ `validateInputInAlphabet()` - input characters are in alphabet and not control characters

**Validation Timing:**
- Configuration: Called from `EngineImpl.configManual()` before creating Code
- Input: Called from `EngineImpl.process()` before processing characters

### 3. Loader Contains XML Structural Validation

**Definition:** XML validation checks the structure and consistency of the XML file itself.

**Loader Validation (LoaderXml.java):**
- ✅ File extension is `.xml`
- ✅ Alphabet size is even
- ✅ Alphabet characters are unique
- ✅ Rotor IDs are contiguous (1..N)
- ✅ Rotor mappings are bijective (full permutations)
- ✅ Notch positions are in range [1, alphabetSize]
- ✅ Reflector IDs are valid Roman numerals (I, II, III, IV, V)
- ✅ Reflector IDs are unique and contiguous
- ✅ Reflector mappings are symmetric (if A→B then B→A)
- ✅ Reflector has no self-mappings (A→A)

### 4. No Duplicated Validation

**Principle:** Each validation rule appears in exactly ONE place.

**Examples:**
- Alphabet membership: ONLY in `EngineValidator.validatePositionsInAlphabet()` and `validateInputInAlphabet()`
- Rotor existence: ONLY in `EngineValidator.validateRotorIdsExistenceAndUniqueness()`
- Even length: Plugboard format check in `ConsoleValidator.validatePlugboardFormat()`, but semantic plugboard validation in `EngineValidator.validatePlugboard()`

### 5. Engine Never Prints; Console Never Validates Machine Logic

**Separation of Concerns:**
- ✅ Engine throws exceptions with detailed messages
- ✅ Console catches exceptions and prints user-friendly messages
- ✅ Console never validates machine logic (rotor existence, alphabet membership, etc.)
- ✅ Engine never prints to stdout/stderr

## Validation Flow Examples

### Example 1: Manual Code Configuration (Command 3)

```
User Input: Rotors "1,2,3", Positions "ABC", Reflector "I"

Console Layer:
  1. Parse rotor IDs → [1, 2, 3]  (InputParsers.parseRotorIds)
  2. Validate positions length = 3 ✓  (ConsoleValidator.ensurePositionsLengthMatches)
  3. Build positions list → ['A', 'B', 'C']  (InputParsers.buildInitialPositions)
  4. Validate reflector choice in range ✓  (ConsoleValidator.ensureReflectorChoiceInRange)
  5. Create CodeConfig([1,2,3], ['A','B','C'], "I")
  6. Call enigma.configManual(config)

Engine Layer (EngineImpl.configManual):
  1. Check spec is loaded ✓
  2. EngineValidator.validateCodeConfig(spec, config):
     a. Null checks ✓
     b. Counts match ROTORS_IN_USE ✓
     c. Rotors [1,2,3] exist in spec ✓
     d. Rotors are unique ✓
     e. Reflector "I" exists in spec ✓
     f. Positions ['A','B','C'] are in alphabet ✓  ← SEMANTIC VALIDATION
     g. Plugboard validation (no-op for now)
  3. Create Code via CodeFactory
  4. Set code on machine
  
Result: ✅ Complete validation without duplication
```

### Example 2: Process Input (Command 5)

```
User Input: "HELLO"

Console Layer:
  1. Read input "HELLO"
  2. Normalize to uppercase "HELLO"
  3. Call enigma.process("HELLO")

Engine Layer (EngineImpl.process):
  1. Validate machine is loaded ✓
  2. Validate machine is configured ✓
  3. EngineValidator.validateInputInAlphabet(spec, "HELLO")
     - Check input not null ✓
     - Check 'H' in alphabet ✓
     - Check 'E' in alphabet ✓
     - Check 'L' in alphabet ✓
     - Check 'L' in alphabet ✓
     - Check 'O' in alphabet ✓
     - No control characters ✓
  4. Process each character through machine
  
Result: ✅ Complete validation without duplication
```

## Changes Made to Achieve This Organization

### Recent Changes (PR #XX)

**Removed from ConsoleValidator:**
- ❌ `validateInputInAlphabet(MachineSpec, String)` - delegated to EngineValidator
- ❌ `validatePositionsInAlphabet(MachineSpec, List<Character>)` - delegated to EngineValidator

**Rationale:** These methods were performing semantic validation (alphabet membership) which violates the "Console contains ONLY format checks" principle. They were also redundant since the Engine already validates alphabet membership in `EngineValidator.validateCodeConfig()` and `EngineValidator.validateInputInAlphabet()`.

**Updated in ConsoleImpl:**
- Removed call to `ConsoleValidator.validatePositionsInAlphabet()` in `handleSetManualCode()`
- Removed call to `ConsoleValidator.validateInputInAlphabet()` in `handleProcessInput()`

### Previous Changes (PRs #86, #85, #84)

Based on documentation and repository memories:
- ✅ Centralized XML validation in LoaderXml
- ✅ Centralized manual config validation in EngineValidator
- ✅ Clear exception hierarchy (InvalidConfigurationException, InvalidMessageException, etc.)
- ✅ Comprehensive error messages with "what, where, how to fix"
- ✅ Complete test coverage for validation rules

## Testing Requirements

### Console Tests
- ✅ Ensure console never calls engine with invalid formatted input (ConsoleValidatorTester)
- ✅ Verify format validation catches: non-numeric commands, mismatched lengths, out-of-range choices
- ✅ Verify format validation does NOT check alphabet membership or machine logic

### Engine Tests
- ✅ Ensure invalid logical input ALWAYS throws, even if console forgot to validate format (EngineValidatorTester)
- ✅ Test all semantic validation rules: rotor existence, uniqueness, position alphabet membership, input alphabet membership
- ✅ Test that validation occurs BEFORE any state changes

### Loader Tests
- ✅ Ensure XML structural validation catches all malformed XML
- ✅ Verify loader validation is independent of Engine/Console

## Validation Layer Compliance Checklist

- [x] XML logical validation centralized in Loader
- [x] Manual config validation centralized in EngineValidator
- [x] Console contains ONLY format checks
- [x] No duplicated code paths validating the same thing
- [x] Engine never prints
- [x] Console never validates machine logic
- [x] Each validation appears in exactly one place
- [x] Console tests ensure it never calls engine with invalid formatted input
- [x] Engine tests ensure invalid logical input ALWAYS throws

## Conclusion

The validation layer organization is now complete and fully compliant with the architectural principles. Each layer has a clear responsibility:

- **Console:** Format validation (syntax)
- **Engine:** Semantic validation (meaning)
- **Loader:** Structural validation (XML)

No validation is duplicated, and each validation rule appears in exactly one place. The Engine always validates semantic correctness even if the Console skips format checks, ensuring robustness.
