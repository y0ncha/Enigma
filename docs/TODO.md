# Phase 1: Project Skeleton & Root Configuration
**Goal:** Establish the aggregator structure and define global build settings.

- [x] **Create Root Directory Structure**
    - Ensure the root folder contains no source code directly (only `pom.xml`, `.gitignore`, `README.md`, etc.).
    - Create sub-directories for each module:
        - `enigma-shared`
        - `enigma-machine`
        - `enigma-loader`
        - `enigma-engine`
        - `enigma-console`

- [x] **Create Root `pom.xml`**
    - Set `groupId` (`patmal.course.enigma`).
    - Set `artifactId` to `enigma-aggregator`.
    - Set `version` (`1.0-SNAPSHOT`).
    - Set `<packaging>pom</packaging>`.
    - Add `<modules>` section listing the 5 modules above.
    - Set properties:
        - `maven.compiler.source`: 21
        - `maven.compiler.target`: 21
        - `project.build.sourceEncoding`: UTF-8
    - Add `<dependencyManagement>` to manage versions of sub-modules and common libraries (like JAXB, Gson) centrally.

# Phase 2: Module Implementation (Bottom-Up)
**Goal:** Migrate code starting from the most independent modules to the most dependent ones.

## Step 2.1: enigma-shared (The Foundation)
- [x] **Create `enigma-shared/pom.xml`**
    - Parent: Root POM.
- [x] **Migrate Code**
    - Move all DTOs (e.g., `CodeConfig`, `MachineState`, `ProcessTrace`).
    - Move Specifications (e.g., `MachineSpec`, `RotorSpec`).
    - Move Exceptions used across boundaries.
    - Move `Alphabet` implementation (shared between machine and spec).

## Step 2.2: enigma-machine (Domain Logic)
- [x] **Create `enigma-machine/pom.xml`**
    - Parent: Root POM.
    - Dependency: `enigma-shared`.
- [x] **Migrate Code**
    - Move `Rotor`, `Reflector`, `Plugboard` implementations.
    - Move the core `MachineImpl` logic.
    - Ensure no references to Engine or Loader exist here.

## Step 2.3: enigma-loader (Data Access)
- [x] **Create `enigma-loader/pom.xml`**
    - Parent: Root POM.
    - Dependency: `enigma-shared`.
    - Dependency: JAXB (`jakarta.xml.bind-api`, `jaxb-runtime`).
- [x] **Migrate Code**
    - Move JAXB generated classes and XML parsing logic.
    - Move `SchemaValidator`.
    - Ensure it returns DTOs/Specs from `enigma-shared`.

## Step 2.4: enigma-engine (Orchestration)
- [x] **Create `enigma-engine/pom.xml`**
    - Parent: Root POM.
    - Dependencies: `enigma-machine`, `enigma-loader`, `enigma-shared`.
- [x] **Migrate Code**
    - Move `EngineImpl`.
    - Move `MachineHistory` logic.
    - Implement the logic that ties Loader and Machine together.

## Step 2.5: enigma-console (User Interface)
- [x] **Create `enigma-console/pom.xml`**
    - Parent: Root POM.
    - Dependencies: `enigma-engine`, `enigma-shared`.
- [x] **Migrate Code**
    - Move `Main.java` (Entry point).
    - Move `Menu`, `InputValidator`, and all `System.out` logic.
    - Ensure it only talks to Engine interface.

# Phase 3: Build & Packaging (The Uber-Jar)
**Goal:** Create the single executable jar `enigma-machine-ex2.jar`.

- [x] **Configure Assembly Plugin**
    - In `enigma-console/pom.xml`, configure `maven-assembly-plugin`.
    - Set the `Main-Class` manifest attribute to `enigma.console.Main`.
    - Configure the `<finalName>` to be exactly `enigma-machine-ex2`.
    - Ensure it includes dependencies from all modules (fat jar).

# Phase 4: Cleanup & Verification
**Goal:** Ensure compliance with strict assignment rules.

- [x] **Clean IDE Artifacts & Package Issues**
    - Delete redundant directories (e.g., `src/enigma` inside `src/main/java`).
    - Fixed incorrect package declarations (removed `main.java.` prefix).
    - Create a `.gitignore` at the root.
- [x] **Verify Architecture Rules**
    - Check imports: machine should NOT import engine.
    - Check printing: Removed `System.out` from engine/machine/loader. Console is now responsible for printing machine state.
- [x] **Run Build**
    - Run `mvn clean package` (Verified with `build` tool).
    - Verify `enigma-machine-ex2.jar` is created in root `target/`.

# Phase 5: Unit Testing & Final Polish
- [x] **Migrate Tests**
    - Move tests to `src/test/java` in their respective modules.
- [ ] **Final Code Review**
    - Ensure Javadoc matches `CONTRIBUTING.md`.
    - Verify all `INSTRUCTIONS.md` requirements are met.
