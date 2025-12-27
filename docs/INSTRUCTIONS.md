# ENIGMA PROJECT MASTER INSTRUCTIONS

This document is the **single source of truth** for all project requirements, architecture constraints, code style, and agent behavior rules for the Enigma Machine project.

---

## 1. Project Overview & Tech Stack
- **Language:** Java 21
- **Build System:** Maven (multi-module)
- **UI:** Console-based (Exercise 2) / Spring Boot REST API (Exercise 3)
- **Language Rule:** All input/output, comments, and documentation must be in **English**. No Hebrew text allowed.
- **Root Path (Spring):** `/enigma`

---

## 2. Module Architecture (Strict)
The project is divided into the following modules. Boundaries must be strictly respected:
- `enigma-machine` — Core domain logic (Rotors, Reflectors, Plugboard).
- `enigma-engine` — Orchestration, validation, history, and state management.
- `enigma-loader` — XML parsing and semantic validation.
- `enigma-console` — UI layer (CLI).
- `enigma-shared` — Immutable DTOs and shared state objects.
- `enigma-server` — Spring Boot controllers and services.

### Architectural Constraints:
- **No Printing:** `machine`, `engine`, and `loader` must never print to console (`System.out`).
- **Isolation:** Never expose internal machine objects outside the `machine` module.
- **Communication:** Use **DTOs** for all data transfer across module boundaries.
- **UI Rules:** UI contains no business logic. Console should catch exceptions and print user-friendly messages.

---

## 3. Maven & Build Rules
- **Aggregator:** Root `pom.xml` must have `packaging=pom` and list all modules.
- **Uber-JAR:** Generate a single runnable jar using `maven-assembly-plugin`.
- **Naming:** 
    - Exercise 2: `enigma-machine-ex2.jar`
    - Exercise 3: `enigma-machine-server-ex3.jar`
- **Standard Layout:** Follow `src/main/java` and `src/test/java`.
- **Cleanliness:** No IDE artifacts (`.idea`, `.iml`, `out`) should be committed.

---

## 4. Code Style & Standards
- **Naming:**
    - Classes: `UpperCamelCase`
    - Methods/Variables: `lowerCamelCase`
    - Constants: `UPPER_SNAKE_CASE`
- **Immutability:** Prefer immutable data structures (Records, Collections.unmodifiable).
- **Complexity:** Avoid long methods and duplicate logic.
- **Formatting:**
    - 100-column soft wrap.
    - `catch`/`else` on a new line after `}`.
    - **Colon Spacing:** Use one space before and after colons in printed output (e.g., `Label : Value`).

---

## 5. Machine Logic & Behavior
- **Rotor Storage:** Stored internally left→right (index 0 is leftmost).
- **Stepping:** Rightmost rotor always steps first. Double-stepping propagates when notch hits window.
- **Rotor Order:** Input is left→right, but internally handled according to specs.
- **Plugboard:** Symmetric mapping. Applied before entering rotors and after exiting.
- **Reset:** Restores original configuration (not the last used one).
- **History:** Tracked per code configuration; includes processed messages and duration (nanoseconds). Resets only on new valid XML load.

---

## 6. Validation Rules & Layer Responsibilities

### Validation Layer Architecture:
1. **Console (Format Validation):**
    - Command parsing (numeric, in range).
    - User input formats (rotor list, positions, reflector choice, plugboard).
    - **Never validates:** Alphabet membership, semantic rules, or XML existence.
2. **Engine (Semantic Validation):**
    - Component existence (Rotor/Reflector IDs).
    - Alphabet membership for all characters.
    - Runtime preconditions (machine loaded before configuration).
    - Plugboard semantics (no duplicates, no self-mapping).
3. **Loader (Structural & Semantic XML Validation):**
    - XML schema and file extension.
    - Alphabet: Even length, unique characters.
    - Rotors: Contiguous IDs, bijective mappings, notch range.
    - Reflectors: Unique Roman IDs, symmetric mappings, no self-mapping.

### Mandatory Rules:

#### XML Loading:
- Alphabet size must be even.
- Rotor IDs must form a continuous sequence `1..N`.
- Rotor mappings must be bijective (no repeats).
- Reflector Roman IDs must be unique.
- No reflector self-mapping.
- `rotors-count` must be within allowed range.
- **Transactional:** Invalid XML must not override current machine state.

#### Code Configuration:
- All Rotor IDs and Reflector IDs must exist in the loaded spec.
- Rotor count must match `rotors-in-use`.
- Positions and Plugboard characters must belong to the machine alphabet.
- Plugboard: Even length, no repetitions, no self-mapping.

---

## 7. Documentation & Javadoc
- **Javadoc:** Minimal and contract-focused. Use `{@inheritDoc}` where applicable.
- **Comments:** Explain "why," not "what." No commented-out code.
- **Agent Documentation Request:**
    - One-line summary.
    - Short parameter/return list.
    - 1–3 line usage example.
    - Max 3 bullet points for invariants/side-effects.

---

## 8. Agent Behavior Rules
Agents working on this repository must:
- Prioritize `INSTRUCTIONS.md` over all other guidance.
- Suggest reorganizations without changing core logic unless requested.
- Ensure all validations are enforced.
- Never violate module boundaries.
- Produce concise, modular, and assignment-compliant code.

---

## 9. Definition of Done
- [ ] Code builds with `mvn clean package`.
- [ ] Uber-jar created with correct name.
- [ ] UI and Logic are strictly separated.
- [ ] All XML and Configuration validations pass.
- [ ] History and Statistics reflect correct behavior.
- [ ] No Hebrew text or printing in logic modules.