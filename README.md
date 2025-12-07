# Enigma Machine — Java E2E
[DeepWiki y0ncha/Enigma](https://deepwiki.com/y0ncha/Enigma)

---

# 1. Project Purpose
This repository contains a complete, modular, assignment-compliant implementation of a generic Enigma machine across all three course exercises:

1. **Exercise 1 — Console Application**
    - Core machine modeling
    - Manual & automatic code configuration
    - Stepping logic
    - Message processing
    - History & statistics
    - Full console interface

2. **Exercise 2 — Maven Modularization**
    - Migration to multi-module Maven
    - Independent modules: machine, engine, loader, console
    - Plugboard support
    - Variable rotor count
    - Uber-jar packaging (`enigma-machine-ex2.jar`)

3. **Exercise 3 — Spring Boot Server (Optional)**
    - REST API exposing machine operations
    - Controllers, services, DTO mapping
    - JSON-based input/output
    - Final standalone server (`enigma-machine-server-ex3.jar`)

This project strictly follows the architectural, behavioral, validation, and formatting requirements defined by the course instructions.

---

# 2. Architecture Overview

## 2.1 Module Structure
```
./
├── enigma-machine      # Core model (rotors, reflectors, alphabet, code runtime)
├── enigma-shared       # Shared DTOs, specs and common types used across modules
├── enigma-engine       # Engine orchestration, validation, history, factories
├── enigma-loader       # XML parsing & validation (schema-valid + application-valid)
├── enigma-console      # Console UI (Exercises 1 & 2)
└── lib                 # Third-party runtime libraries (JAXB/JAX-B etc.)
```

> Note: A `enigma-server` module (Spring Boot) is optional for Exercise 3 — if present it will appear alongside the modules above.

### Module Responsibilities

**enigma-machine**
- Pure domain model: Rotor, Reflector, Alphabet, Plugboard, Code and component implementations
- Implements stepping and signal routing logic
- Contains no printing or UI I/O
- Designed to be reusable and testable from other modules

**enigma-shared**
- Shared DTOs, small records and specification types used by engine/loader/console
- MachineSpec / RotorSpec / ReflectorSpec and other immutable data contracts
- Keeps module coupling low by centralizing cross-cutting types

**enigma-engine**
- Passive coordinator and validator
- Performs all runtime validations (XML vs spec, manual code setup, message input, etc.)
- Holds current machine state, history recording and tracing helpers
- Produces DTOs for UI/server layers, and exposes a minimal API

**enigma-loader**
- JAXB-based XML parsing and schema validation
- Converts XML → MachineSpec and enforces structural rules (bijectivity, notch, reflector pairs)
- Keeps loader-specific parsing logic isolated from engine runtime

**enigma-console**
- Console-based user interface and manual test runners
- Responsible for printing results and ASCII debugging tables
- Uses engine and shared DTOs but performs no business logic itself

**lib**
- Contains third-party jars required at runtime (kept out of Maven for coursework convenience)

---

# 3. Validation Rules (Strict Compliance)

These validations fully implement both the schema and assignment rules.

## 3.1 XML-Level Validation
The loader ensures:

- Alphabet size is **even**
- Rotor IDs are exactly **1..N without gaps**
- Rotor mappings are **bijective**
    - No duplicates in left or right column
- Notch index is within alphabet range
- Reflector Roman IDs are **unique** and one of: I, II, III, IV, V
- Reflector cannot map **input to itself**
- Number of reflect mappings = alphabetSize / 2
- `rotors-count` (Exercise 2) ≤ number of defined rotors
- All characters must be ASCII; forbidden characters: newline, tab, ESC
- XML errors never crash the system; engine remains unchanged on invalid XML

## 3.2 Manual Code Configuration Validation
- Rotor IDs must match the allowed set
- Rotor order provided left→right but internally stored right→left
- Initial positions must match alphabet characters
- Reflector must exist
- Plugboard rules:
    - Even-length string
    - No letter repeated
    - No letter mapped to itself

## 3.3 Input Message Validation
- All characters must belong to the machine alphabet
- System provides clear, actionable error messages

---

# 4. Machine Behavior

- Rightmost rotor **always steps**
- Double-stepping according to notch logic
- For each input character:
  ```
  plugboard → rotors (right→left) → reflector → rotors (left→right) → plugboard
  ```
- Rotor positions are **not reset** after processing
- `Reset` returns the machine to the **original code configuration**

---

# 5. History & Statistics

Engine records:

- Original code
- All code configurations used
- All processed messages
- Duration per processing (in nanoseconds)
- Grouping by code configuration
- Auto-reset only when a **valid** XML is loaded

---

# 6. How to Build & Run

## Exercise 1 / 2 — Console
```
mvn clean install
java -jar enigma-machine-ex2.jar
```

## Exercise 3 — Server
```
mvn clean install
java -jar enigma-machine-server-ex3.jar
```

Server root: `http://localhost:8080/enigma`


## 6.1 Running sanity test runners (named-by-config)

For quick end-to-end verification we provide test runners under `src/test` that are intentionally named after the `CodeConfig` they exercise. This makes it simple to re-run a specific configuration and inspect the results.

- `test.enigma.engine.sanitypaper.MultiWordTester_1_2_3_ODX_I`
  - CodeConfig: `<1,2,3><ODX><I>` — runs the "sanity-paper" cases (paper appendix)
- `test.enigma.engine.sanitysamll.MultiWordTester_3_2_1_CCC_I`
  - CodeConfig: `<3,2,1><CCC><I>` — runs the smaller sanity cases (sanity-small)

How to compile and run a named runner (from repo root):

1) Compile all sources:

```bash
find . -name "*.java" > sources.txt
javac -cp "lib/*" -d out @sources.txt
```

2) Run the specific runner (example):

```bash
java -cp "out:lib/*" test.enigma.engine.sanitypaper.MultiWordTester_1_2_3_ODX_I
```

Notes:
- Each runner re-applies its `CodeConfig` before each case so every message starts from the same rotor positions; outputs include Input / Expected / Actual and a final summary.
- The test class file names follow the pattern: `MultiWordTester_<rotorIds>_<positions>_<reflectorId>` where `rotorIds` is a comma-separated list (left→right), `positions` are the starting characters, and `reflectorId` is the reflector identifier.


---

# 7. README Requirements for Grading
Per course instructions:

- This file documents all main modules and decisions
- Includes all assumptions, validations, and flows
- Instructor can run the system using only this document
- System is fully testable using the provided Postman collection

---

# 8. Repository Assumptions
- No internal objects are exposed outside the engine
- DTOs are used for all communication
- No UI logic outside `enigma-console`
- No hardcoded paths (except temporary test files, when instructed)

---

# 9. Contact
This README serves as the canonical reference for evaluating correctness, compliance, and structure.

---

# 10. Integration Documentation

## 10.1 Console-Engine Integration

The console module provides a user-friendly command-line interface that communicates with the engine through a well-defined DTO-based API. Key integration points:

### Module Responsibilities
- **Console:** Format-level input validation, user interaction, output formatting
- **Engine:** Semantic validation, orchestration, state management
- **Machine:** Core Enigma mechanics and signal processing

### Data Flow
```
User Input → Console (format check) 
          → Engine (semantic validation)
          → Machine (encryption)
          → DTOs (results)
          → Console (display)
```

### Key DTOs Used
- `MachineSpec` - Machine configuration metadata
- `CodeConfig` - Rotor/reflector configuration
- `CodeState` - Current machine state with positions and notch distances
- `MachineState` - Comprehensive snapshot (counts, original/current config)
- `ProcessTrace` - Message processing results with signal traces

### Position Convention
All position values throughout the system use **left→right** ordering:
- User input "ABC" means: A=leftmost rotor, B=middle, C=rightmost
- `CodeConfig` stores positions as chars in left→right order
- Machine displays positions in left→right order

