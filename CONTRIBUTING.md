# CONTRIBUTING — documentation & code comment style

## Goal

Keep public documentation and inline comments minimal, consistent and actionable.
This file codifies the short style we use in this repository so contributors write
uniform, useful docs without over-explaining implementation details.

## High-level rules

- Be concise. One-line summaries for types; short paragraphs for behavior.
- Prefer short inline comments (//) for *why* or non-obvious decisions — not for
  describing what the code literally does.
- Put detailed design rationale in the README or a short design doc; keep
  Javadoc focused on API contract (inputs/outputs/preconditions).
- Use "// --- Flow helpers: machine creation and random code generation ---------" as sections separators.

## JavaDoc guidelines

- Class summary: 1–2 short sentences describing responsibility.
- Method summary: 1 short sentence describing effect.
- Use these tags where applicable: @param, @return, @throws. Keep descriptions short.
- For public API methods include short preconditions (caller responsibility). Use a
  bullet list inside the method Javadoc when necessary.
- Avoid long history notes, long examples, or language other than English.
- Ordering convention: document left→right vs right→left only where it matters
  (e.g. CodeConfig vs runtime Code). Put that note in class-level JavaDoc for
  the factory/engine that relies on it.

## Inline comment guidelines

- Use // comments for small clarifications:
  - Explain the reason ("why") not the mechanics ("what").
  - Keep to one line when possible; two short lines max.
- Use block comments (/* ... */) only for short file headers or license blocks.
- Do NOT leave commented-out code; remove it or add a short note in git history.

## Examples (good)

// reverse because runtime expects right->left ordering
Collections.reverse(list);

/**
 * Create a Code from a validated MachineSpec and CodeConfig.
 * Preconditions: spec != null, config contains exactly 3 rotor ids (left->right).
 */
public Code create(MachineSpec spec, CodeConfig config) { ... }

## Examples (avoid)

// this loops over the items and adds them to the result
for (...) { ... }
/* old implementation kept for debugging */

## Formatting and tone

- Use plain, neutral English.
- Keep sentences short and imperative when giving instructions.
- Keep line length reasonable (wrap at ~100 columns).

## Validation and where it lives

- Validation of XML/Specs/configs belongs to the Engine layer (EngineImpl).
  Factories should assume inputs are valid and only construct runtime objects.
  Document this responsibility in both Engine and factory JavaDoc.

## Pull requests & review checklist

- Include 1–2 sentence summary of the change and why.
- Add/modify JavaDoc only where behavior or API changed.
- Run the project's checks (IDE/build) and ensure no new compile errors.
- Reviewers: verify the public contract (Javadoc) matches implementation.

If you want stricter automated checks (Javadoc style linter, commit hooks) we can
add them; open an issue or a PR proposing specifics.

Thank you for keeping docs short and useful — concise comments are more likely
to be read and maintained.

