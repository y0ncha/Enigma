# AGENTS.md

This file defines mandatory guardrails for agents working in this repository.
It is aligned to the Enigma assignment flow, with Part 3 (server/API) as the
current delivery target and Part 4 groundwork requirements included.

## 1) Source of truth and priority
- Primary source: `/docs/Enigma - 3.0 V3.pdf`.
- Contract source for API behavior and format: `/.openAPI/api/openapi.yaml`.
- Working repository constraints: `/docs/INSTRUCTIONS.md`, `/README.md`,
  `/docs/ex3-api-contract.md`.
- If sources conflict:
  1. User explicit instruction in current chat.
  2. PDF assignment requirements.
  3. `/.openAPI/api/openapi.yaml` for all API contract decisions.
  4. `docs/INSTRUCTIONS.md`.
  5. Existing code style/conventions in repository.

## 2) Current target (Part 3) and scope
- Deliver and preserve a Spring Boot REST server for Enigma under `/enigma`.
- Keep module boundaries strict:
  - `enigma-machine`: pure domain mechanics only.
  - `enigma-engine`: orchestration + semantic validation.
  - `enigma-loader`: XML parsing + XML/structure validation.
  - `enigma-dal`: JPA entities/repositories only.
  - `enigma-sessions`: application/session services over engine + DAL.
  - `enigma-api`: controllers + API DTO mapping + error mapping.
  - `enigma-app`: Spring boot runtime wiring/config.
  - `enigma-console`: CLI UI only.
  - `enigma-shared`: immutable DTOs/state objects.
- No business logic in controllers or UI.
- Do not expose internal machine objects across module boundaries.

## 3) Validation responsibilities (must not drift)
- API/Console:
  - Transport and input format checks only.
  - Clear request parsing and user-facing validation messages.
- Engine/Sessions:
  - Semantic rules, preconditions, and state transition validity.
  - Runtime constraints (loaded/configured/open session, alphabet membership).
- Loader:
  - XML schema + structural machine rules.
  - Invalid XML must not override current valid machine state.

## 4) API behavior rules for Part 3
- STRICT COMPLIANCE with `/.openAPI/api/openapi.yaml` is mandatory.
- Treat OpenAPI as canonical for:
  - Path and method.
  - Query parameter names/casing (example: `sessionID` exact spelling).
  - Request/response body schema fields and field casing.
  - Content types (`application/json`, `text/plain`, `multipart/form-data`).
  - HTTP status codes per endpoint.
- Preserve route groups and base path semantics under `/enigma`:
  - `/load`, `/session`, `/config`, `/process`, `/history`.
- Keep DTO contracts stable unless user explicitly requests a contract change.
- Ensure consistent status mapping for validation/not-found/conflict/server errors.
- Keep timestamp/session/machine fields deterministic and serializable.
- History and processing records must remain queryable and coherent per scope.
- No "almost matching" payloads are allowed; response format must match OpenAPI.

## 5) Persistence and DB safety (hard constraints)
- NO DDL is allowed.
- Do not run, generate, suggest, or auto-apply schema-changing SQL:
  `CREATE`, `ALTER`, `DROP`, `TRUNCATE`, `RENAME`, migrations, or
  any schema evolution scripts.
- Allowed DB operations:
  - Read-only queries.
  - DML only (`SELECT`, `INSERT`, `UPDATE`, `DELETE`) within existing schema.
- If a task seems to require schema change:
  - Stop and report mismatch.
  - Propose application-level workaround inside current schema.
  - Wait for explicit user override before any schema discussion continues.

## 6) Coding and architecture rules
- Java 21, Maven multi-module, keep modules decoupled.
- Prefer immutable DTOs/records and explicit mapping layers.
- Keep methods focused; avoid duplicated validation logic.
- No console printing/logging side effects in domain modules.
- Keep exceptions meaningful and mapped to API responses predictably.
- Maintain left-to-right rotor/order conventions exactly as documented.
- Preserve plugboard, reset, and history semantics from current implementation.

## 7) Testing and verification requirements
- Every non-trivial change must include verification:
  - Unit/service tests where feasible.
  - API-level sanity checks for changed endpoints/flows.
- Minimum checks before completion:
  - `mvn -q -DskipTests compile` at least for touched modules.
  - If practical, `mvn test` for affected modules.
- Never claim tests passed if not executed.

## 8) Important carryovers from later assignment parts
- Keep contracts stable and backward-compatible by default.
- Keep state transitions explicit and auditable (session open/close/config/process).
- Keep error payloads consistent and machine-readable.
- Build for deterministic behavior under repeated calls and retries.
- Avoid hidden side effects and global mutable state where possible.

## 9) Part 4 groundwork (must be prepared now)
- Design now for future expansion without breaking Part 3:
  - Keep service interfaces small, explicit, and implementation-agnostic.
  - Isolate orchestration logic in `enigma-sessions` from transport concerns.
  - Keep identifiers (`machine`, `session`, record IDs) stable and canonical.
  - Ensure operations are idempotent where applicable.
  - Maintain clear transaction boundaries and conflict handling.
  - Keep history/events append-only in behavior (even if storage is simple).
  - Add extension points for async/background processing without API rewrites.
  - Avoid tight coupling to a single client flow; support multiple clients later.
- Any Part 3 change should be evaluated for Part 4 readiness:
  - "Will this force contract breakage later?"
  - "Will this block concurrency/multi-session scaling?"
  - "Will this require schema change to evolve?"

## 10) Agent execution behavior in this repo
- Make minimal, targeted changes; avoid unrelated refactors.
- Do not silently change API contracts or endpoint semantics.
- Before finishing API changes, verify endpoint signatures and payload formats
  against `/.openAPI/api/openapi.yaml`.
- Document assumptions when requirements are ambiguous.
- If blocked by environment/tooling, state exactly what is missing.
- Keep all comments/docs/code in English.
