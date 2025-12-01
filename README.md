# Enigma Machine — End-to-End Java Project

A compact, modular implementation of an Enigma-style cipher used as a three-stage course project. The repository evolves from a console application (Exercise 1) to a multi-module layout (Exercise 2) and — optionally — a Spring Boot REST server (Exercise 3).

---

## Project overview

This project implements the components required to model and run an Enigma-like machine:
- Core machine primitives (rotors, reflectors, keyboard/alphabet).
- Engine that orchestrates loading, configuration and message processing.
- XML loader that builds machine specifications from declarative XML descriptions.
- Console user interface (and optional server interface).

The codebase is organized to keep modelling, orchestration, loading and UI concerns separate.

---

## Exercises (high level)

- Exercise 1 — Console UI
  - Single-module console application implementing machine behaviour, manual and automatic code configuration, stepping logic, and basic statistics.

- Exercise 2 — Modular project
  - Split to distinct modules so each area can evolve independently.
  - Current repository modules reflect this modularization (see "Folder layout" below).

- Exercise 3 — Server (optional / course-specific)
  - A Spring Boot REST server that exposes machine operations (load XML, configure, process text, history/statistics).
  - This module may not be present in every repository snapshot; check for `app-server/` or `server/`.

---

## Technologies

- Java (project uses modern Java; course uses Java 11+ / Java 21 depending on configuration)
- JAXB for XML parsing (loader is JAXB-based)
- SecureRandom for sampling
- (Optional) Spring Boot for REST server

---

## Folder layout (actual repo)

Top-level modules and important folders in this repository:

```
./
├─ CONTRIBUTING.md
├─ LICENSE
├─ README.md
├─ enigma-machine/     # core machine model (rotors, reflectors, alphabet, code)
├─ enigma-engine/      # engine, loader (XML), factories, validation, history
├─ enigma-console/     # console UI and main runnable for exercises 1 & 2
├─ lib/                # auxiliary JARs used on classpath (JAXB, etc.)
└─ ...
```

Notes:
- The XML loader implementation lives under `enigma-engine/src/.../loader` (package `enigma.engine.components.loader`).
- There may be other course artifacts (tests, example XML files) under the modules' `src/` folders.

---

## Module dependency graph

The following diagram shows the intended dependency flow (consumer ← provider):

```
enigma-machine    ←    enigma-engine    ←    enigma-console
      ↑                  ↑                    ↑
      └── (loader code inside enigma-engine)   └── (optional server)
                         ↑
                       shared (optional DTOs)
```

- `enigma-machine` contains the low-level model and runtime primitives.
- `enigma-engine` depends on `enigma-machine` and implements loading, validation, factories and orchestration.
- `enigma-console` (CLI) depends on `enigma-engine` to run exercises.
- The loader (XML parsing / generated JAXB artifacts) is currently part of `enigma-engine` (not a separate module).
- An `app-server` / `server` module (if present) acts as another consumer of `enigma-engine`.

---

## Build & run (concise)

Choose one of the approaches below depending on how you prefer to work (IDE, direct javac/java, or Maven if you add poms).

1) Run from an IDE (recommended for development)
- Import the project as a multi-module project (or open modules separately).
- Ensure `lib/` JARs are added to each module's classpath (JAXB jars are required for XML loader).
- Run the `Main` class in `enigma-console` (likely `enigma.console.Main` or `Console` entry point).

2) Run with javac/java (manual classpath)
- Compile modules and include `lib/*.jar` on the classpath. Example (run from project root):

```bash
# compile (example - adapt source paths to your environment)
javac -d out -cp "lib/*" $(find enigma-machine enigma-engine enigma-console -name "*.java")

# run the console main (replace MainClass with the real FQN)
java -cp "out:lib/*" enigma.console.Main
```

3) Run with Maven (if you add module pom.xml files)
- If you convert the modules into Maven modules, build all with:

```bash
mvn clean install
```

- Then run the console jar or the server jar from its module `target/` directory:

```bash
java -jar enigma-console/target/<console-artifact>.jar
# or
java -jar app-server/target/<server-artifact>.jar
```

Notes:
- The loader expects XML machine description files (see `enigma-engine/src/resources/xml` or similar example files). Keep those XMLs on the classpath or pass them as runtime arguments.
- Many course snapshots are easiest to run from an IDE because the project does not always include ready-made pom.xml files.

---

## Important project concerns (course requirements)

- XML-based machine loading and strict validation are central to the exercises. The loader code (JAXB + validation) is part of `enigma-engine`.
- The core modelling (rotors, reflectors, alphabet, code) is in `enigma-machine` and should remain independent of UI concerns.
- The console UI implements manual and random code configuration flows; the server (if present) exposes the same operations via REST.

---

## Contributing and documentation

- Follow `CONTRIBUTING.md` for coding, JavaDoc and comment style used in the course.
- Keep Javadoc concise and use `{@code ...}` for inline code or XML tags in docs.

---

If anything in this README contradicts your local repository layout, please open the relevant module folder and adapt the module name (for example: `server` vs `app-server`)—this README aims to be accurate to the repository snapshot but stays intentionally concise.
