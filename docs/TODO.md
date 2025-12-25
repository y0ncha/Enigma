# Plugboard Implementation Tasks

## Context Key Points (must stay aligned)
- **Modules & responsibilities**
  - `enigma-machine`: pure domain logic (rotors/reflector/plugboard + encryption flow). **No printing.**
  - `enigma-engine`: orchestration + **all semantic validations** + history/statistics. **No printing.**
  - `enigma-console`: UI only (prompt/parse/format-level checks). **All printing happens here.**
  - `enigma-shared`: DTOs + specs that cross module boundaries.
- **Rule of thumb:** Console may check “is input empty / even length / basic format”, but **Engine validates machine rules** (alphabet membership, duplicates, self-mapping, etc.).
- **Plugboard behavior:** applied **twice** per character:
  1) before entering rotors, 2) after exiting rotors.
- **Reset behavior:** reset restores **original configured code** (must include plugboard state).
- **History/stats:** code identity should include plugboard (otherwise different configs collide under the same “code”).

---

## Task 1: Add Plugboard to Domain (enigma-machine)
- [ ] Create `Plugboard` value object (immutable).
  - Suggested internal structure: `int[] mapping` where unmapped letters map to themselves.
- [ ] API:
  - `int swap(int index)`
  - `boolean isIdentity()`
- [ ] Integrate into machine encryption pipeline:
  - `swap -> rotors forward -> reflector -> rotors backward -> swap`
- [ ] Default plugboard must be identity (empty configuration).

**Acceptance**
- Encrypting with identity plugboard behaves exactly like “no plugboard”.
- Plugboard swap is symmetric (A<->B).

---

## Task 2: Extend Shared Configuration / State DTOs (enigma-shared)
- [ ] Extend code/config DTO (whatever your system uses: `CodeConfig`, `CodeState`, etc.) with plugboard representation:
  - `String plugboardPairs` (empty = not configured) OR a structured list of pairs.
- [ ] Extend “machine state” DTO printed in command #2 to include plugboard field.

**Acceptance**
- Engine can report plugboard state without exposing internal machine objects.

---

## Task 3: Centralize Plugboard Validations in Engine (enigma-engine)
Implement a single validation/build flow in Engine:
- [ ] `Plugboard parseAndBuildPlugboard(String pairs, Alphabet alphabet)` (method name flexible)
- [ ] Semantic rules:
  - empty string is valid
  - even length
  - every char in alphabet
  - each char appears at most once
  - no self-mapping pairs like `AA`
- [ ] Throw `IllegalArgumentException` with clear, non-technical messages.

**Acceptance**
- Console doesn’t “know” any plugboard semantic rules besides minimal format checks.
- Invalid plugboard never changes current machine configuration.

---

## Task 4: Manual Configuration Flow Updates (Engine + Console)
- [ ] Update manual config DTO/input to include plugboard.
- [ ] Update console “manual config” command to collect plugboard as 4th input:
  1) rotors list
  2) positions string
  3) reflector roman
  4) plugboard pairs string (can be empty)
- [ ] Engine applies validation + config update atomically.

**Acceptance**
- Manual config sets plugboard and it affects encryption immediately.
- Reset restores the manual plugboard to the original code.

---

## Task 5: Add/Update Dedicated Plugboard Command (Console -> Engine)
(If you already planned “command #3 plugboard config”, implement it now.)
- [ ] Console reads plugboard string and delegates to Engine:
  - `engine.configurePlugboard(pairs)` or via `configManual(...)` update
- [ ] Engine validates + updates current configuration and history if needed.

**Acceptance**
- Plugboard can be changed independently (if assignment allows) OR through full manual config (if that’s your chosen UX). Either way, behavior is consistent and validated.

---

## Task 6: Update “Present Machine State” Output (Console)
- [ ] Include plugboard in command #2 output:
  - If identity: `Plugboard: Not configured`
  - Else: `Plugboard: <pairs>`
- [ ] Make sure this matches your existing UI formatting style.

**Acceptance**
- Tester can clearly see plugboard state in machine snapshot.

---

## Task 7: Random Configuration Includes Plugboard (enigma-engine)
- [ ] Extend `configRandom()` to generate a random plugboard:
  - choose `k` pairs where `0 <= k <= alphabetSize/2`
  - pick `2k` distinct letters and pair them
- [ ] Store resulting plugboard in current + history.

**Acceptance**
- Random config always produces a valid plugboard.
- Sometimes produces empty plugboard too (k = 0).

---

## Task 8: History/Statistics Key Includes Plugboard (enigma-engine)
- [ ] Ensure “used codes” grouping / key includes plugboard (alongside rotors order, window letters, reflector, etc.).

**Acceptance**
- Two configs that differ only by plugboard are tracked separately in stats/history.

---

## Task 9: Tests (enigma-engine + optional machine unit tests)
- [ ] Validation tests:
  - `""` valid
  - `"AB"` valid
  - `"A"` invalid (odd length)
  - `"AA"` invalid (self-map)
  - `"ABAC"` invalid (duplicate)
  - contains non-alphabet char invalid
- [ ] Behavior test:
  - With plugboard `"AB"`, ensure swapping occurs before+after rotor processing (at least one integration test).

**Acceptance**
- Tests cover both invalid inputs and correctness of plugboard effect.

---

## Task 10: Close Remaining Plugboard `// TODO`s
- [ ] Scan repo for `TODO` mentioning plugboard and link each to one of the tasks above.
- [ ] Remove TODO comments once implemented.

**Acceptance**
- No open TODOs related to plugboard remain.