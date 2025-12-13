# CONTRIBUTING — Code Style, Formatting & Documentation Rules
### Applies to All Project Modules

This document defines the mandatory conventions contributors must follow.  
It is strict by design to maintain alignment with course requirements.

---

# 1. General Coding Rules

- **Java 21**
- Class names — `UpperCamelCase`
- Methods & variables — `lowerCamelCase`
- Constants — `UPPER_SNAKE_CASE`
- Avoid long methods; keep logic readable
- Prefer immutability (Alphabet, Mapping, CodeConfiguration, DTOs)
- Keep classes small with clear responsibility
- No "god classes"
- Avoid unnecessary static utility classes
- No unused imports

---

# 2. Architecture Rules (Mandatory)

### 2.1 Layer Separation
- `machine` — core logic
- `engine` — validation, orchestration, history
- `loader` — XML parsing + structural validation
- `console` — UI printing only
- `server` — controllers + services only

### 2.2 Forbidden
- No printing in machine / engine / loader
- No Hebrew output
- No exposure of internal machine objects
- No business logic in UI
- No hardcoding XML paths
- No global shared mutable state

---

# 3. Javadoc Rules (Strict)

### 3.1 Class-Level
- One–two sentences describing responsibility
- State direction conventions (left→right vs right→left) only where strictly relevant

### 3.2 Method-Level
- One-line functional summary
- Tags: `@param`, `@return`, `@throws` (short descriptions only)
- Document preconditions clearly
- When overriding or inheriting a method's documentation in a subclass or implementation, use `{@inheritDoc}` in the overriding method's Javadoc to inherit the parent/interface documentation; add only the subclass-specific differences as short sentences (do not duplicate the parent's content).

### 3.3 Inline Comments
- Use `//` for *why*, not for *what*
- Never leave commented-out code
- Keep all sentences short, clear, direct

---

# 4. Validation Rules and Layer Responsibilities

All validation logic is distributed across three layers with strict separation of concerns:

### Validation Layer Architecture

**Console (Format Validation):**
- Command parsing (numeric, in range)
- Rotor ID list format (comma-separated integers)
- Position string format (A-Z characters, correct length)
- Reflector choice format (numeric, in range)
- Plugboard format (even length)
- **Never validates:** Alphabet membership, rotor/reflector existence, semantic rules

**Engine (Semantic Validation):**
- Rotor IDs exist in loaded spec and are unique
- Reflector ID exists in loaded spec
- Position characters are in machine alphabet
- Input characters are in machine alphabet
- Plugboard semantics (no duplicates, no self-mapping, alphabet membership)
- State preconditions (machine loaded before config, configured before processing)
- **Never validates:** XML structure, format parsing

**Loader (Structural Validation):**
- File extension is `.xml`
- Alphabet: even length, unique characters, ASCII only
- Rotor IDs: contiguous sequence 1..N
- Rotor mappings: bijective (complete permutations)
- Notch positions: within alphabet range
- Reflector IDs: Roman numerals (I, II, III, IV, V), contiguous, unique
- Reflector mappings: symmetric, no self-mapping, complete coverage
- Rotors-in-use: ≤ available rotors
- **Never validates:** User configurations, runtime input

### Validation Principles

1. **Single Point of Truth:** Each constraint validated at exactly one layer
2. **No Duplication:** Same rule never checked in multiple layers
3. **Clear Error Messages:** What's wrong, where it occurred, how to fix
4. **Fail Fast:** Invalid input rejected before any state changes
5. **Transactional:** Invalid XML never modifies engine state

### Forbidden Characters
The following characters are explicitly rejected in alphabets and input:
- Newline (`\n`), Tab (`\t`), ESC (ASCII 27)
- All non-printable control characters (ASCII 0-31, 127)

---

# 5. DTO Rules
- DTOs are immutable containers
- They encapsulate engine output for UI/server layers
- Must not expose internal machine objects
- Must contain only what external layers need
- Engine never returns domain objects — only DTOs

---

# 6. Exceptions & Error Handling

- Internal methods may throw exceptions
- Engine and server surfaces must catch them and convert to clean, human-readable messages
- Never propagate internal stack traces
- Error messages must:
    - Explain what is wrong
    - Where it occurred
    - How to fix

---

# 7. Formatting Rules (Strict)

- 100-column soft wrap
- Consistent indentation
- Blank lines between logical sections
- Use section dividers only in this style:

```java
// ---------------------------------------------------------
// Section Title
// ---------------------------------------------------------
```

### 7.1 Try/Catch and If/Else Block Formatting

- Place the `catch` or `else` clause on a new line immediately after the closing brace `}` of the preceding `try` or `if` block.
- Do not place `catch`/`else` on the same line as the closing brace.
- Keep indentation and spacing consistent with the surrounding code.

Example (required style):

```
       try {
            enigma.reset();
            System.out.println("Code has been reset to original configuration");
        }
        catch (EngineException e) {
            System.out.println("Failed to reset code : " + e.getMessage());
        }
```

- The same rule applies to `if/else` chains:

```
        if (condition) {
            // ...
        }
        else {
            // ...
        }
```

### 7.2 Colon Spacing in Printed Messages

- When printing labels and values, always include **one space before and one space after the colon** (e.g., `Label : Value`). This is intentional and required, even though it differs from standard English style.
- This rule applies to all user-facing output (console, logs shown to users, examples in documentation).

Example (required style):

```
    Note : This test requires XML test files to be present.
    Advanced rotor indices : [0, 1, 2]
```

---

# 8. Pull Request Checklist

Before submitting:

- Code compiles
- Javadoc updated
- No redundant logic
- No UI or printing in engine/loader
- All validations enforced
- Tests (manual or automated) run successfully
