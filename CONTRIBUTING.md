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

### 3.3 Inline Comments
- Use `//` for *why*, not for *what*
- Never leave commented-out code
- Keep all sentences short, clear, direct

---

# 4. Validation Rules (Must Match Engine Behavior)

All validation logic must reside in the engine and loader:

### XML Validation
- Even alphabet size
- Rotor IDs consecutive
- Rotor mappings bijective
- No reflector maps X→X
- Roman reflector IDs unique
- `rotors-count` <= available rotors
- Forbidden chars: newline, tab, ESC
- Invalid XML leaves current machine untouched

### Code Configuration Validation
- Rotor count must match spec
- Rotor IDs must exist
- Initial positions valid in alphabet
- Reflector must exist
- Plugboard: even length, no duplicates, no self-mapping

### Input Validation
- All characters must be in alphabet

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

---

# 8. Pull Request Checklist

Before submitting:

- Code compiles
- Javadoc updated
- No redundant logic
- No UI or printing in engine/loader
- All validations enforced
- Tests (manual or automated) run successfully  