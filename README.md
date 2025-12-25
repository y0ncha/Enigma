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

## 2.1 Architectural Principles

The system follows a **layered architecture** with clear separation of concerns:

1. **Domain Layer** (enigma-machine): Core mechanical model and encryption logic
2. **Shared Layer** (enigma-shared): Common data contracts and DTOs
3. **Service Layer** (enigma-engine): Orchestration, validation, and state management
4. **Data Access Layer** (enigma-loader): XML parsing and structural validation
5. **Presentation Layer** (enigma-console): User interface and interaction

**Key Design Principles:**
- **Unidirectional Dependencies:** Lower layers never depend on higher layers
- **DTO-Based Communication:** Modules communicate through immutable data transfer objects
- **Single Responsibility:** Each module has one well-defined purpose
- **No Cross-Cutting Concerns:** Printing, validation, and business logic are strictly separated

## 2.2 Module Structure

```
./
├── enigma-machine      # Core domain model (rotors, reflectors, alphabet, code)
├── enigma-shared       # Shared DTOs, specs, and common types
├── enigma-engine       # Orchestration, validation, history, and factories
├── enigma-loader       # XML parsing and structural validation
├── enigma-console      # Console UI (Exercises 1 & 2)
└── lib                 # Third-party runtime libraries (JAXB, etc.)
```

> **Note:** An optional `enigma-server` module (Spring Boot) may be present for Exercise 3.

## 2.3 Module Responsibilities

### enigma-machine (Domain Layer)
**Purpose:** Pure domain model implementing the mechanical behavior of Enigma components.

**Responsibilities:**
- Define and implement: Rotor, Reflector, Alphabet, Plugboard, Code, Machine
- Execute stepping logic (rightmost rotor always steps, double-stepping on notches)
- Process signal routing through rotors and reflector
- Generate detailed signal traces for debugging and verification
- Maintain rotor positions during processing

**Does NOT:**
- Perform validation (assumes all inputs are valid)
- Print or display output
- Parse XML or load specifications
- Manage history or statistics
- Handle user input

**Dependencies:** enigma-shared (DTOs only)

### enigma-shared (Shared Layer)
**Purpose:** Shared data contracts used for inter-module communication.

**Responsibilities:**
- Define immutable DTOs: CodeConfig, CodeState, MachineState, ProcessTrace
- Define specification records: MachineSpec, RotorSpec, ReflectorSpec
- Define trace records: SignalTrace, RotorTrace, ReflectorTrace
- Define history records: MessageRecord
- Maintain zero dependencies on other enigma modules

**Does NOT:**
- Contain business logic
- Perform validation
- Implement encryption
- Interact with users or files

**Dependencies:** None (Java standard library only)

### enigma-engine (Service/Orchestration Layer)
**Purpose:** Coordinate machine operations, validate configurations, and maintain history.

**Responsibilities:**
- Load machine specifications via Loader
- Validate code configurations (semantic checks against loaded spec)
- Validate input messages (alphabet membership, forbidden characters)
- Create Code instances via CodeFactory
- Coordinate machine processing operations
- Record and organize processing history by original code
- Generate random valid configurations
- Provide machine state snapshots as DTOs
- Reset machine to original configuration

**Does NOT:**
- Parse XML directly (delegates to Loader)
- Implement mechanical encryption (delegates to Machine)
- Display output or interact with users (delegates to Console)
- Perform format validation (delegates to Console)

**Dependencies:** enigma-machine, enigma-loader, enigma-shared

### enigma-loader (Data Access/Parsing Layer)
**Purpose:** Load and validate machine specifications from XML files.

**Responsibilities:**
- Parse XML files using JAXB
- Validate XML schema compliance
- Validate alphabet structure (even length, unique characters, ASCII only)
- Validate rotor specifications (contiguous IDs, bijective mappings, notch range)
- Validate reflector specifications (Roman numeral IDs, symmetric mappings)
- Convert XML to MachineSpec/RotorSpec/ReflectorSpec DTOs
- Provide clear, actionable error messages for validation failures

**Does NOT:**
- Validate code configurations (runtime, not load-time)
- Validate user input messages
- Create machine components (delegates to Engine's factory)
- Process encryption
- Interact with users

**Dependencies:** enigma-shared (spec DTOs), JAXB libraries

### enigma-console (Presentation Layer)
**Purpose:** Provide command-line interface for user interaction.

**Responsibilities:**
- Display interactive menu
- Read and parse user commands
- Perform format-level validation (parsing, basic type checks)
- Call engine operations (delegate business logic)
- Display results and error messages in user-friendly format
- Print machine data, history, and traces
- Manage menu enabling/disabling based on engine state
- Handle retry loops for invalid input

**Does NOT:**
- Validate machine specifications (Loader's responsibility)
- Validate semantic rules (Engine's responsibility)
- Implement encryption logic (Machine's responsibility)
- Maintain history or state (Engine's responsibility)
- Parse XML (Loader's responsibility)

**Dependencies:** enigma-engine, enigma-shared

## 2.4 Data Flow

### Loading a Machine
```
XML File → Loader (parse & validate) → MachineSpec → Engine (store)
```

### Configuring the Machine
```
User Input → Console (format validation) → CodeConfig → Engine (semantic validation) → Code Factory → Machine
```

### Processing a Message
```
User Input → Console (format) → Engine (validate) → Machine (encrypt) → ProcessTrace → Console (display)
```

### Key Characteristics
- **Validation occurs at multiple layers:** Format (Console) → Semantic (Engine) → Structural (Loader)
- **DTOs flow between layers:** No internal objects are exposed
- **Errors propagate upward:** Lower layers throw exceptions, Console displays them
- **State is centralized:** Engine maintains all configuration and history state

---

# 3. Constraints and Validation Rules

This section documents the constraints that govern the Enigma machine system. These constraints are enforced through validation at different layers (Loader, Engine, Console) to ensure correctness and consistency throughout the application lifecycle.

## 3.1 Constraint Categories

### Domain Constraints
Domain constraints define the fundamental properties of the machine components and their valid states.

**Alphabet Constraints:**
- Must have even length (required for symmetric reflector pairings)
- All characters must be unique (no duplicates allowed)
- All characters must be ASCII (printable characters only)
- Minimum size: 2 characters (1 reflector pair)

**Rotor Constraints:**
- Rotor IDs must form a contiguous sequence starting at 1 (e.g., 1, 2, 3, 4, 5)
- Each rotor has exactly two columns (right and left) defining its wiring
- Each column must be a complete permutation of the alphabet (bijective mapping)
- Each rotor has exactly one notch position within the alphabet range
- Minimum of 1 rotor, maximum limited only by practical considerations

**Reflector Constraints:**
- Reflector IDs must be Roman numerals following the sequence I, II, III, IV, V
- Reflector IDs must be contiguous starting from I (no gaps allowed)
- Each reflector defines a symmetric mapping: if A→B then B→A
- No character may map to itself (A→A is forbidden)
- Must define exactly alphabetSize/2 pairs (complete coverage of alphabet)

**Plugboard Constraints (Exercise 2):**
- Plugboard string must have even length (character pairs)
- No character may appear more than once
- No character may map to itself
- All characters must exist in the machine alphabet

### Primary and Candidate Keys

**Machine Specification:**
- **Primary Key:** File path (uniquely identifies a loaded machine specification)
- **Invariant:** At most one machine specification is loaded at any time

**Rotor Specification:**
- **Primary Key:** Rotor ID (integer, unique within a machine specification)
- **Candidate Key:** None (rotor ID is the sole identifier)
- **Invariant:** Rotor IDs form a contiguous sequence 1..N

**Reflector Specification:**
- **Primary Key:** Reflector ID (Roman numeral string, unique within specification)
- **Candidate Key:** None (reflector ID is the sole identifier)
- **Invariant:** Reflector IDs form a contiguous sequence I, II, III, ...

**Code Configuration:**
- **Composite Key:** (Rotor IDs, Initial Positions, Reflector ID, Plugboard)
- **Purpose:** Uniquely identifies a machine configuration state
- **Usage:** Groups processing history by original configuration

### Referential Integrity Constraints

**Code Configuration to Machine Specification:**
- Number of selected rotor IDs must equal spec.rotorsInUse
- All selected rotor IDs must exist in spec.rotors
- Selected rotor IDs must be unique (no rotor used twice)
- Selected reflector ID must exist in spec.reflectors
- All position characters must exist in spec.alphabet
- All plugboard characters must exist in spec.alphabet

**Message Processing to Machine State:**
- Machine specification must be loaded before configuration
- Machine must be configured before processing messages
- All input characters must exist in the current machine's alphabet

### Logical Invariants

**State Transitions:**
- Loading a new machine clears all configuration and history
- Configuring the machine records the "original code" for reset operations
- Processing messages advances rotor positions but never modifies the machine specification
- Reset restores positions to the original code but preserves history
- Terminate clears all state but does not exit the application

**Rotor Ordering:**
- Rotors are indexed left→right throughout the system (index 0 = leftmost)
- User input, internal storage, and display all use the same left→right convention
- Signal processing iterates right→left (forward) and left→right (backward)
- Stepping always begins at the rightmost rotor (highest index)

**History Grouping:**
- All messages processed after a configuration are grouped by the original code
- Original code state captures rotor IDs and initial positions at configuration time
- History persists through reset operations
- History is cleared only on loadMachine() or terminate()

## 3.2 Validation Layer Separation

The system enforces constraints through a three-layer validation architecture:

### Loader Layer (XML Structural Validation)
Validates machine specification structure at load time:
- File extension is `.xml`
- Alphabet: even length, unique characters, ASCII only
- Rotor IDs: contiguous sequence 1..N
- Rotor mappings: bijective (complete permutations)
- Notch positions: within alphabet range [1, alphabetSize]
- Reflector IDs: valid Roman numerals (I, II, III, IV, V), contiguous, unique
- Reflector mappings: symmetric, no self-mapping, complete coverage
- Rotors-in-use count: ≤ number of defined rotors

### Engine Layer (Semantic Validation)
Validates configurations and inputs against the loaded specification:
- Code configuration: rotor count, rotor existence, rotor uniqueness, reflector existence
- Position characters: membership in machine alphabet
- Input characters: membership in machine alphabet, no forbidden control characters
- Plugboard: even length, no duplicates, no self-mapping, alphabet membership
- State preconditions: machine loaded before configuration, machine configured before processing

### Console Layer (Format Validation)
Validates user input format before delegation:
- Command IDs: numeric, within valid range
- Rotor list: parseable as comma-separated integers
- Position string: correct length, A-Z characters only
- Reflector choice: numeric, within available range
- Plugboard: even length (semantic checks delegated to engine)

**Principle:** Each constraint is validated at exactly one layer. No duplication.

## 3.3 Forbidden Characters

The following characters are explicitly forbidden in machine alphabets and input messages:
- Newline (`\n`, ASCII 10)
- Tab (`\t`, ASCII 9)
- Escape (ESC, ASCII 27)
- All non-printable control characters (ASCII 0-31, 127)

**Rationale:** Control characters can cause display issues and are not part of the original Enigma character set.

## 3.4 Error Handling Guarantees

**Transactional Load:**
- Invalid XML never modifies the current machine state
- If loading fails, the previously loaded machine remains active
- If no machine was loaded, the engine remains in uninitialized state

**Clear Error Messages:**
All validation failures provide:
- **What** is wrong (clear description of the violated constraint)
- **Where** the error occurred (rotor ID, reflector ID, character position)
- **How** to fix (actionable guidance for correction)

**No Silent Failures:**
- All constraint violations throw exceptions
- No warnings or partial acceptance of invalid data
- The system state is never left inconsistent

---

# 4. Machine Behavior and Operations

This section describes the operational behavior of the Enigma machine during encryption, stepping, and state management.

## 4.1 Encryption Process

For each input character, the machine performs the following transformations in sequence:

```
Input Character
    ↓
Plugboard transformation (optional, Exercise 2)
    ↓
Forward pass: Right→Left through rotors
    ↓
Reflector transformation (symmetric mapping)
    ↓
Backward pass: Left→Right through rotors
    ↓
Plugboard transformation (optional, Exercise 2)
    ↓
Output Character
```

**Key Principle:** The encryption is deterministic. Given the same initial state and input character, the output will always be the same.

## 4.2 Stepping Mechanism

**Before processing each character**, the machine steps rotors according to these rules:

1. **Rightmost rotor always steps** (advances by one position)
2. **Notch-triggered stepping**: When a rotor reaches its notch position after stepping, it triggers the rotor to its left to step
3. **Double-stepping**: A rotor at its notch position steps both itself and the rotor to its left (this causes the middle rotor to sometimes step on consecutive characters)

**Stepping Order:**
- Stepping begins at the rightmost rotor (highest index in the rotor array)
- Propagates leftward when notches are engaged
- Occurs before signal processing, ensuring positions are updated first

**Position Visibility:**
- Rotor positions represent the character currently visible in the machine's window
- Positions are displayed left→right to match physical machine appearance
- Position changes are permanent until reset or reconfiguration

## 4.3 State Management Operations

### Configuration
**Manual Configuration** (`configManual`):
- Sets specific rotor IDs, positions, reflector, and plugboard
- Records the configuration as the "original code" for later reset
- Clears positions from any previous configuration
- Adds a new group to history for tracking messages under this configuration

**Automatic Configuration** (`configRandom`):
- Generates a cryptographically random valid configuration
- Uses all available rotors and reflectors from the loaded specification
- Delegates to manual configuration for validation and setup

### Reset
**Reset Operation** (`reset`):
- Returns rotor positions to their **original values** (as set during configuration)
- Does **NOT** change rotor/reflector selection
- Does **NOT** clear processing history
- Does **NOT** reset the message counter
- **Purpose:** Allows re-encryption of messages from the same starting point

### Terminate
**Terminate Operation** (`terminate`):
- Clears the loaded machine specification
- Clears the current code configuration
- Clears all processing history
- Resets the message counter to zero
- Does **NOT** call `System.exit()` or terminate the application
- **Purpose:** Returns the engine to an uninitialized state for loading a new machine

## 4.4 State Transition Diagram

```
[Uninitialized]
    ↓ loadMachine(path)
[Machine Loaded, Not Configured]
    ↓ configManual(config) or configRandom()
[Machine Configured, Ready for Processing]
    ↓ process(message)
[Processing Complete, Positions Advanced]
    ↓ reset()
[Machine Configured, Positions Restored]
    
At any point:
    terminate() → [Uninitialized]
    loadMachine(newPath) → [Machine Loaded, Not Configured] (clears config and history)
```

## 4.5 Determinism and Repeatability

**Guaranteed Behavior:**
- Same configuration + same input → same output (always)
- Reset operation allows exact replay of previous encryptions
- History tracking preserves the complete sequence of operations
- No randomness in encryption (randomness only in automatic configuration generation)

---

# 5. History and Statistics

The engine maintains a complete history of all processing operations, organized by the original code configuration.

## 5.1 History Structure

**Grouping by Original Code:**
- Each call to `configManual()` or `configRandom()` creates a new "original code" entry
- Original code captures: rotor IDs, initial positions, reflector ID, plugboard
- All messages processed afterward are grouped under this original code
- Multiple original codes can exist if the machine is reconfigured during a session

**Message Records:**
Each processed message is recorded with:
- Input string (the plaintext or ciphertext)
- Output string (the encrypted or decrypted result)
- Processing duration in nanoseconds (for performance analysis)

## 5.2 History Persistence

**History is Preserved:**
- Through reset operations (positions change, history remains)
- Through multiple configurations (each adds a new group)
- Throughout the application session

**History is Cleared:**
- When loading a new machine specification (`loadMachine`)
- When terminating the engine (`terminate`)
- **Never** during normal processing or reset operations

## 5.3 Statistics Tracked

- **Total Processed Messages:** Count of all `process()` calls since last clear
- **Messages Per Configuration:** Count per original code group
- **Processing Duration:** Nanosecond precision timing for each message
- **Configuration Changes:** Number of times the code has been set

## 5.4 History Use Cases

- **Verification:** Confirm correct encryption/decryption results
- **Performance Analysis:** Identify timing patterns or bottlenecks
- **Debugging:** Trace the sequence of operations and configurations
- **Testing:** Validate against known test vectors
- **Auditing:** Maintain a record of all encryption operations

---

# 6. Building, Running, and Testing

## 6.1 Prerequisites

- **Java 21 or higher** (JDK required for compilation)
- **Maven 3.8+** (for dependency management and building)
- **Operating System:** Windows, macOS, or Linux

## 6.2 Building the Project

### Maven Build (Recommended)
```bash
mvn clean install
```

This command:
1. Cleans previous build artifacts
2. Compiles all source code across modules
3. Runs any configured tests
4. Packages the application as executable JARs

### Manual Compilation (Alternative)
```bash
# Compile all sources
find . -name "*.java" > sources.txt
javac -cp "lib/*" -d out @sources.txt
```

## 6.3 Running the Application

### Exercise 1 & 2 — Console Application
```bash
java -jar enigma-machine-ex2.jar
```

**Expected Output:**
- Interactive console menu
- Options to load machine, configure, process messages, view history
- User prompts for input at each step

### Exercise 3 — REST API Server (Optional)
```bash
java -jar enigma-machine-server-ex3.jar
```

**Server Configuration:**
- **Default Port:** 8080
- **Base URL:** `http://localhost:8080/enigma`
- **API Documentation:** Available via Swagger/OpenAPI at `/swagger-ui.html`

## 6.4 Running Tests

### Sanity Test Runners

The project includes named test runners that exercise specific configurations with known test vectors. These are useful for quick verification and regression testing.

**Available Test Runners:**

1. **Paper Appendix Tests** — Configuration `<1,2,3><ODX><I>`
   ```bash
   java -cp "out:lib/*" test.enigma.engine.sanitypaper.MultiWordTester_1_2_3_ODX_I
   ```
   Tests the configuration documented in the course paper appendix.

2. **Sanity Small Tests** — Configuration `<3,2,1><CCC><I>`
   ```bash
   java -cp "out:lib/*" test.enigma.engine.sanitysamll.MultiWordTester_3_2_1_CCC_I
   ```
   Tests a smaller validation set with different rotor ordering.

**Test Runner Naming Convention:**
- Pattern: `MultiWordTester_<rotorIds>_<positions>_<reflectorId>`
- `rotorIds`: Comma-separated rotor IDs in left→right order (e.g., `1_2_3`)
- `positions`: Starting positions as characters (e.g., `ODX`)
- `reflectorId`: Reflector identifier (e.g., `I`)

**Test Output Format:**
```
Test Case: <name>
Input    : <input string>
Expected : <expected output>
Actual   : <actual output>
Result   : PASS/FAIL

Summary: X passed, Y failed
```

### Maven Tests
```bash
mvn test
```

Runs all unit and integration tests across all modules.

## 6.5 Configuration Files

### XML Machine Specifications

Machine specifications are loaded from XML files with the following structure:
- **Location:** User-specified path (absolute or relative)
- **Extension:** Must be `.xml`
- **Schema:** Defined by course requirements (alphabet, rotors, reflectors)

**Example Usage:**
```
> 1. Load machine configuration from XML file
Enter path to XML file: machines/enigma-standard.xml
✓ Machine loaded successfully!
```

### Required XML Elements
- `ABC`: Alphabet definition (even length, unique characters)
- `Rotors`: Rotor specifications (contiguous IDs, bijective mappings)
- `Reflectors`: Reflector specifications (Roman numeral IDs, symmetric mappings)
- `rotors-count` (Exercise 2): Number of rotors in use

---

# 7. Terminology and Conventions

## 7.1 Key Definitions

**Machine Specification (MachineSpec):**
The complete definition of an Enigma machine loaded from XML, including alphabet, available rotors, available reflectors, and rotors-in-use count. Immutable once loaded.

**Code Configuration (CodeConfig):**
A specific selection and arrangement of rotors, reflector, positions, and plugboard that defines the machine's starting state for encryption. This is what users configure manually or automatically.

**Original Code:**
The code configuration as recorded at the moment of configuration. Used as the reset target and history grouping key.

**Current Code State:**
The machine's current configuration including positions that may have advanced during processing. Positions differ from original code after processing messages.

**Rotor Position:**
The character currently visible in the machine's window for a specific rotor. Changes with each character processed.

**Notch:**
A position on a rotor that, when reached, triggers the next rotor to the left to advance. Enables the double-stepping mechanism.

**Stepping:**
The advancement of rotor positions before processing each character. Rightmost rotor always steps; others step based on notch positions.

**Double-Stepping:**
A mechanism where a rotor at its notch position steps both itself and the next rotor to its left, causing consecutive stepping in some cases.

**Signal Trace:**
A detailed record of a character's transformation path through the machine, including all rotor transformations, reflector mapping, and intermediate values.

**History:**
A chronological record of all processed messages, grouped by original code configuration, including input, output, and processing duration.

## 7.2 Ordering Conventions

**Left→Right Indexing:**
Throughout the system, rotors are indexed left→right:
- Index 0 = leftmost rotor (visible in left window position)
- Index size-1 = rightmost rotor (steps every character)

**User Input Format:**
All user input follows left→right convention:
- Rotor IDs: "1,2,3" means rotor 1 is leftmost, rotor 3 is rightmost
- Positions: "ABC" means 'A' is leftmost position, 'C' is rightmost position

**Signal Processing Direction:**
Signal flows right→left (forward pass) then left→right (backward pass), but array indexing remains left→right throughout.

## 7.3 Validation Terminology

**Format Validation:**
Syntactic checks on input structure (e.g., numeric, correct length, parseable). Performed by Console layer.

**Semantic Validation:**
Logical checks on input meaning relative to machine specification (e.g., rotor exists, character in alphabet). Performed by Engine layer.

**Structural Validation:**
Checks on XML file structure and internal consistency (e.g., bijective mappings, symmetric reflectors). Performed by Loader layer.

---

# 8. Compliance and Documentation Standards

## 8.1 Course Requirement Compliance

This README documents all aspects required for course evaluation:
- Complete module architecture and responsibilities
- All validation rules and constraints
- Machine behavior and operations
- Build and run instructions
- Testing procedures

## 8.2 Self-Sufficiency

**Documentation Principle:** A reviewer should be able to understand the complete system by reading this README without examining source code.

**What This README Provides:**
- Architectural overview with module boundaries
- Complete constraint documentation (domain, keys, integrity, invariants)
- Validation layer separation and responsibilities
- Operation semantics (configuration, processing, reset, terminate)
- History and statistics tracking
- Build, run, and test instructions
- Terminology and conventions

## 8.3 Assumptions and Design Decisions

**Rotor Ordering:**
Left→right indexing throughout matches user mental model and simplifies reasoning about configurations.

**Validation Layering:**
Three-layer validation (Console/Engine/Loader) prevents duplication while ensuring robustness at each level.

**DTO-Based Communication:**
All inter-module communication uses immutable DTOs to prevent coupling and ensure thread-safety.

**History Grouping by Original Code:**
Grouping messages by original configuration (not current state) allows tracking all work done from a specific starting point.

**Transactional Loading:**
Invalid XML never modifies engine state, ensuring consistency and predictability.

**No Silent Failures:**
All constraint violations throw exceptions with clear messages. No warnings or partial acceptance.

---

# 9. Repository Maintenance

## 9.1 Contributing

See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for:
- Code style and formatting rules
- Javadoc conventions
- Architecture boundaries
- Pull request checklist

## 9.2 Documentation Structure

- **README.md** (this file): High-level overview, architecture, constraints, operations
- **Module READMEs**: Detailed module-specific documentation
  - `enigma-machine/README.md`: Domain model details
  - `enigma-engine/README.md`: Engine operations and validation
  - `enigma-loader/README.md`: XML parsing and structural rules
  - `enigma-console/README.md`: Console interface and user interaction
  - `enigma-shared/README.md`: DTO and specification definitions
- **docs/** directory: Specialized topic documentation
  - `VALIDATION_LAYER_ORGANIZATION.md`: Validation architecture
  - `ROTOR_ORDERING_CONVENTION.md`: Indexing and ordering details
  - Additional review and analysis documents

## 9.3 Contact and Support

This README serves as the canonical reference for evaluating correctness, compliance, and structure. For questions or clarifications, refer to:
1. This README for high-level understanding
2. Module READMEs for implementation details
3. Source code Javadoc for API specifics

---