# Enigma Machine (Java E2E)

[DeepWiki y0ncha/Enigma](https://deepwiki.com/y0ncha/Enigma)

Multi-module Java implementation of the Enigma assignment across:
- Exercise 1: console interaction and machine operations
- Exercise 2: modular Maven architecture
- Exercise 3: Spring Boot REST server with persistent session/history data

## Repository Layout

This repository currently builds **9 Maven modules**:

| Module | Purpose |
|---|---|
| `enigma-shared` | Shared DTOs, specs, and machine/code state records |
| `enigma-machine` | Core Enigma mechanics (rotors, reflector, plugboard, stepping, signal path) |
| `enigma-loader` | XML parsing and structural validation into machine specs |
| `enigma-engine` | Orchestration, semantic validation, processing, and history logic |
| `enigma-console` | CLI app for Ex1/Ex2 flows |
| `enigma-dal` | JPA entities and repositories |
| `enigma-sessions` | Session/application services over engine + persistence |
| `enigma-api` | REST controllers, API DTOs, and exception mapping |
| `enigma-app` | Spring Boot launcher and runtime configuration |

## Architecture (High-Level)

```text
XML file -> enigma-loader -> MachineSpec
                         -> enigma-engine (domain orchestration)

Console path:
user -> enigma-console -> enigma-engine -> enigma-machine

Server path:
HTTP -> enigma-api -> enigma-sessions -> enigma-engine + enigma-dal -> PostgreSQL
```

Validation responsibilities are split by layer:
- Loader: XML/schema + structural machine rules
- Engine/Sessions: runtime semantics and state preconditions
- API/Console: transport/input format and request parsing

## Prerequisites

- JDK 21+
- Maven 3.8+
- PostgreSQL (required for server mode)

Default server DB config (`enigma-app/src/main/resources/application.properties`):
- URL: `jdbc:postgresql://localhost:5432/enigma`
- Username: `postgres`
- Password: `enigma`

## Build

From repository root:

```bash
mvn clean install
```

This produces runnable artifacts at:
- `target/enigma-machine-ex2.jar` (console)
- `target/enigma-machine-server-ex3.jar` (Spring Boot server)

## Run

### Console (Exercises 1-2)

```bash
java -jar target/enigma-machine-ex2.jar
```

### Server (Exercise 3)

```bash
java -jar target/enigma-machine-server-ex3.jar
```

Server defaults:
- Port: `8080`
- Context path: `/enigma`
- Base URL: `http://localhost:8080/enigma`

## REST API Summary (Current Controllers)

All routes below are relative to `/enigma`.

### Machine loading
- `POST /load` (multipart form-data, field name: `file`) -> load/register XML machine
- `GET /load` -> list loaded machines

### Session lifecycle
- `POST /session` (JSON body: `{"machine":"<machineName>"}`) -> create session
- `DELETE /session?sessionID=<uuid>` -> close session
- `GET /session/{sessionId}` -> get one session
- `GET /session` -> list sessions

### Configuration
- `GET /config?sessionID=<uuid>&verbose=<true|false>` -> config/machine state
- `PUT /config/manual` (JSON body) -> set manual code
- `PUT /config/automatic?sessionID=<uuid>` -> random configuration
- `PUT /config/reset?sessionID=<uuid>` -> reset to original code

### Processing
- `POST /process?input=<text>` -> process message using the **latest open session**

### History
- `GET /history?sessionID=<uuid>` -> history by session
- `GET /history?machineName=<name>` -> history by machine

Constraint: exactly one of `sessionID` or `machineName` must be provided on `/history`.

## API Payload Notes

Current API style in code:
- Request parameters for many operations use `sessionID` query params
- Manual configuration uses the Ex3-style rotor/plug JSON structure
- Error responses are returned as:

```json
{
  "error": "<message>"
}
```

## Postman and API Contract Files

- Collection: `postman/collections/enigma-ex3-collection.json`
- Contract lock/notes: `docs/ex3-api-contract.md`

## Testing Status

- `mvn test` is available, but there are currently limited automated JUnit tests.
- Manual test assets exist under:
  - `enigma-console/src/test/resources/`
  - `enigma-loader/src/test/resources/`
- A manual console test harness exists at:
  - `enigma-console/src/test/java/enigma/console/Tester.java`

## Useful Docs in `docs/`

- `docs/INSTRUCTIONS.md`
- `docs/VALIDATION_LAYER_ORGANIZATION.md`
- `docs/ROTOR_ORDERING_CONVENTION.md`
- `docs/VALIDATION_VERIFICATION.md`
- `docs/XML_VALIDATION_REVIEW.md`

## Notes

- The root README is intentionally focused on the current runnable system and command paths.
- Detailed per-module behavior is documented in module-level READMEs where present.
