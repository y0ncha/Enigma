# enigma-loader

**Module Purpose**: XML parsing and structural validation for Enigma machine specifications.

## Overview

This module is responsible for loading Enigma machine specifications from XML files and validating them against structural and semantic rules. It translates XML representations into strongly-typed `MachineSpec` objects that can be used by the engine.

## Module Layer

**Layer**: Data Access/Parsing Layer  
**Dependencies**: enigma-shared (for spec DTOs), JAXB libraries  
**Used By**: enigma-engine

## Responsibilities

### What This Module DOES
- ✅ Parse XML files using JAXB
- ✅ Validate XML schema compliance
- ✅ Validate alphabet structure (even length, unique characters)
- ✅ Validate rotor specifications (IDs 1..N, bijective mappings, notch range)
- ✅ Validate reflector specifications (Roman numeral IDs, symmetric mappings)
- ✅ Convert XML to MachineSpec/RotorSpec/ReflectorSpec DTOs
- ✅ Provide clear, actionable error messages

### What This Module DOES NOT DO
- ❌ Validate code configurations (engine's responsibility)
- ❌ Validate user input messages (engine's responsibility)
- ❌ Create machine components (engine's factory responsibility)
- ❌ Process encryption (machine's responsibility)
- ❌ Interact with users (console's responsibility)

## Structural Constraints Validated by Loader

The loader enforces **structural constraints** on XML files to ensure machine specifications are internally consistent and valid. These constraints are validated once at load time, before the specification is passed to the engine.

### XML File Constraints
- File must exist and be accessible
- File extension must be `.xml` (case-insensitive)
- XML must be well-formed (valid syntax)
- XML must conform to JAXB schema expectations

### Alphabet Constraints
Defines the character set for the machine:

- **Non-empty**: At least 2 characters required (minimum one reflector pair)
- **Even length**: Required for symmetric reflector pairings (each character has a partner)
- **Unique characters**: No duplicates allowed within the alphabet
- **ASCII only**: All characters must be ASCII (no Unicode, no control characters)

**Rationale for Even Length:** Reflectors map characters in pairs. An odd-length alphabet would leave one character unpaired, violating the reflector symmetry constraint.

### Rotor Constraints
Each rotor specification must satisfy:

#### Rotor ID Sequence
- **Contiguous from 1**: Rotor IDs must form sequence 1, 2, 3, ..., N (no gaps)
- **Starting at 1**: First rotor must be ID 1 (not 0 or any other number)
- **No duplicates**: Each rotor ID appears exactly once

**Valid Example:** 1, 2, 3, 4, 5  
**Invalid Examples:**
- 1, 2, 4, 5 (gap at 3)
- 0, 1, 2, 3 (must start at 1)
- 1, 2, 2, 3 (duplicate 2)

**Rationale:** Contiguous IDs simplify rotor selection and ensure consistent numbering across the specification.

#### Rotor Wiring (Bijectivity)
Each rotor has two columns (right and left) that define its wiring:

- **Complete permutation**: Each column must contain every alphabet character exactly once
- **No duplicates**: No character appears multiple times in a column
- **No missing characters**: All alphabet characters must appear in each column
- **Both columns bijective**: Both right and left columns must be complete permutations

**Rationale:** Bijective mappings ensure reversibility. The signal can traverse forward and backward through the rotor with deterministic transformations.

#### Notch Position
- **1-based index**: Notch position is in range [1, alphabetSize]
- **Within alphabet**: Notch references a valid position in the alphabet

**Rationale:** Notch triggers stepping of adjacent rotors. It must reference a valid alphabet position.

## Key Component

### LoaderXml
**Purpose**: JAXB-based XML parser and validator.

**Constructor**:
```java
public LoaderXml()                 // Default: 3 rotors in use
public LoaderXml(int rotorsInUse)  // Custom rotor count (Exercise 2)
```

**Main Method**:
```java
MachineSpec loadSpecs(String filePath) throws EnigmaLoadingException
```

**Loading Process**:
1. Validate file path and extension (.xml)
2. Unmarshal XML → JAXB generated objects
3. Validate and extract alphabet
4. Validate and extract rotors
5. Validate and extract reflectors
6. Build and return MachineSpec

**Transactional Guarantee:** If any validation fails, no MachineSpec is created. The caller receives an exception with a clear error message. Partial or invalid specifications are never returned.

## Structural Constraints Validated by Loader

The loader enforces **structural constraints** on XML files to ensure machine specifications are internally consistent and valid. These constraints are validated once at load time, before the specification is passed to the engine.

### XML File Constraints
- File must exist and be accessible
- File extension must be `.xml` (case-insensitive)
- XML must be well-formed (valid syntax)
- XML must conform to JAXB schema expectations

### Alphabet Constraints
Defines the character set for the machine:

- **Non-empty**: At least 2 characters required (minimum one reflector pair)
- **Even length**: Required for symmetric reflector pairings (each character has a partner)
- **Unique characters**: No duplicates allowed within the alphabet
- **ASCII only**: All characters must be ASCII (no Unicode, no control characters)

**Rationale for Even Length:** Reflectors map characters in pairs. An odd-length alphabet would leave one character unpaired, violating the reflector symmetry constraint.

**Example Valid Alphabets**:
- "ABCDEFGHIJKLMNOPQRSTUVWXYZ" (26 chars, even ✓)
- "ABCDEF" (6 chars, even ✓)

**Example Invalid Alphabets**:
- "ABCDEFG" (7 chars, odd ✗)
- "ABBA" (duplicate 'A' and 'B' ✗)

### Rotor Constraints
Each rotor specification must satisfy:

#### Rotor ID Sequence
- **Contiguous from 1**: Rotor IDs must form sequence 1, 2, 3, ..., N (no gaps)
- **Starting at 1**: First rotor must be ID 1 (not 0 or any other number)
- **No duplicates**: Each rotor ID appears exactly once

**Valid Example:** 1, 2, 3, 4, 5  
**Invalid Examples:**
- 1, 2, 4, 5 (gap at 3)
- 0, 1, 2, 3 (must start at 1)
- 1, 2, 2, 3 (duplicate 2)

**Rationale:** Contiguous IDs simplify rotor selection and ensure consistent numbering across the specification.

#### Rotor Wiring (Bijectivity)
Each rotor has two columns (right and left) that define its wiring:

- **Complete permutation**: Each column must contain every alphabet character exactly once
- **No duplicates**: No character appears multiple times in a column
- **No missing characters**: All alphabet characters must appear in each column
- **Both columns bijective**: Both right and left columns must be complete permutations

**Rationale:** Bijective mappings ensure reversibility. The signal can traverse forward and backward through the rotor with deterministic transformations.

**Example** (alphabet "ABCD"):
```xml
<BTE-Positioning>
    <Right>A</Right>
    <Left>B</Left>
</BTE-Positioning>
<BTE-Positioning>
    <Right>B</Right>
    <Left>C</Left>
</BTE-Positioning>
<BTE-Positioning>
    <Right>C</Right>
    <Left>D</Left>
</BTE-Positioning>
<BTE-Positioning>
    <Right>D</Right>
    <Left>A</Left>
</BTE-Positioning>
```
Right column: A, B, C, D (all present ✓)  
Left column: B, C, D, A (all present ✓)

#### Notch Position
- **1-based index**: Notch position is in range [1, alphabetSize]
- **Within alphabet**: Notch references a valid position in the alphabet

**Example**: For alphabet size 26, notch can be 1-26.

**Rationale:** Notch triggers stepping of adjacent rotors. It must reference a valid alphabet position.
- ✅ No duplicates

**Valid**: 1, 2, 3, 4, 5  
**Invalid**: 1, 2, 4, 5 (gap at 3)  
**Invalid**: 1, 2, 2, 3 (duplicate 2)  
**Invalid**: 0, 1, 2, 3 (must start at 1)

#### Rotor Mappings (Bijectivity)
Each rotor has two columns (right and left) defined in XML `<BTE-Positioning>` elements. Both columns must be **full permutations** of the alphabet (bijectivity):
- ✅ Every alphabet character appears exactly once in each column
- ✅ No missing characters
- ✅ No duplicate characters

**Example** (alphabet "ABCD"):
```xml
<BTE-Positioning>
    <Right>A</Right>
    <Left>B</Left>
</BTE-Positioning>
<BTE-Positioning>
    <Right>B</Right>
    <Left>C</Left>
</BTE-Positioning>
<BTE-Positioning>
    <Right>C</Right>
    <Left>D</Left>
</BTE-Positioning>
<BTE-Positioning>
    <Right>D</Right>
    <Left>A</Left>
</BTE-Positioning>
```
Right column: A, B, C, D (all present ✓)  
Left column: B, C, D, A (all present ✓)

#### Notch Position
- ✅ 1-based index
- ✅ Must be in range [1, alphabetSize]

**Example**: For alphabet size 26, notch can be 1-26.

### Reflector Constraints
Each reflector specification must satisfy:

#### Reflector ID Format
- **Roman numerals only**: Must be one of I, II, III, IV, V
- **Contiguous from I**: Must start with I and continue without gaps
- **No duplicates**: Each reflector ID appears exactly once

**Valid Examples:** I | I, II | I, II, III  
**Invalid Examples:**
- I, III (gap at II)
- II, III, IV (must start at I)
- I, I, II (duplicate I)

**Rationale:** Roman numerals follow historical Enigma convention. The loader validates format so the engine doesn't need to parse Roman numerals — it just checks existence in the spec.

#### Reflector Wiring (Symmetry and Completeness)
Reflectors define symmetric pairwise mappings:

- **Symmetric**: If A→B, then B→A (automatically enforced by construction)
- **No self-mapping**: No character maps to itself (A→A is forbidden)
- **Complete coverage**: All alphabet characters must be paired
- **Exact pair count**: Must define exactly alphabetSize/2 pairs

**How Symmetry is Enforced:**
The loader constructs reflector mappings by processing pairs:
```java
mapping[inputIndex] = outputIndex;
mapping[outputIndex] = inputIndex;  // Automatic symmetry
```
This construction makes asymmetric mappings impossible.

**Example** (alphabet "ABCD", size 4 → 2 pairs needed):
```xml
<BTE-Reflect>
    <Input>A</Input>
    <Output>B</Output>
</BTE-Reflect>
<BTE-Reflect>
    <Input>C</Input>
    <Output>D</Output>
</BTE-Reflect>
```
Result: A↔B, C↔D (symmetric ✓, complete ✓, 2 pairs ✓)

**Rationale for Symmetry:** Historical Enigma reflectors were physically symmetric due to their wiring construction. Symmetry also ensures the machine is self-reciprocal (encrypting twice returns the original message).

**Rationale for No Self-Mapping:** If A→A, the reflector would return the character unchanged, which breaks the Enigma security model (a letter can never encrypt to itself).

### Rotors-In-Use Count (Exercise 2)
- **Must be positive**: ≥ 1 rotor required
- **Must not exceed available**: ≤ number of defined rotors
- **Default**: 3 rotors (if not specified)

**Valid Examples:**
- 5 rotors defined, rotors-in-use = 3 ✓
- 5 rotors defined, rotors-in-use = 5 ✓

**Invalid Examples:**
- 5 rotors defined, rotors-in-use = 6 ✗ (exceeds available)
- rotors-in-use = 0 ✗ (must be positive)

## Wire Ordering Preservation

**Critical Design Principle:** The loader must **NEVER** reorder wires or change XML-defined order.

### Rotor Wiring Order
Rotor columns are stored in **XML row order** (top→bottom as parsed):
- First `<BTE-Positioning>` → row 0 (top of rotor)
- Second `<BTE-Positioning>` → row 1
- Last `<BTE-Positioning>` → row N-1 (bottom of rotor)

This order is preserved exactly in `RotorSpec`:
```java
public record RotorSpec(
    int id,
    char[] rightColumn,  // top→bottom order from XML
    char[] leftColumn,   // top→bottom order from XML
    int notch
)
```

### Reflector Wiring Order
Reflector pairs are processed in **XML order** and stored in a mapping array:
```java
int[] mapping = new int[alphabetSize];
for (each <BTE-Reflect> pair in XML order) {
    mapping[inputIndex] = outputIndex;
    mapping[outputIndex] = inputIndex;
}
```

**Why This Matters:**
- The mechanical model depends on exact wire positioning
- Reordering would produce incorrect encryption results
- The loader is the authoritative source for wiring order
- Any transformation or sorting breaks correctness

**Invariant:** XML order = RotorSpec order = Machine internal order (preserved throughout the system)

## Error Handling

**Example** (alphabet "ABCD", 4 chars → 2 pairs needed):
```xml
<BTE-Reflect>
    <Input>A</Input>
    <Output>B</Output>
</BTE-Reflect>
<BTE-Reflect>
    <Input>C</Input>
    <Output>D</Output>
</BTE-Reflect>
```
Result: A↔B, C↔D (symmetric ✓, complete ✓)

### Rotors-In-Use Count (Exercise 2)
- ✅ Must be ≤ number of defined rotors
- ✅ Must be > 0

**Example**: If XML defines 5 rotors, `rotors-count` can be 1-5.

## Wire Ordering — Critical Design Point

**The loader must NEVER reorder wires or change XML-defined order.**

### Rotor Wiring
Rotor columns are stored in **XML row order** (top→bottom as parsed):
- First `<BTE-Positioning>` → row 0 (top of rotor)
- Second `<BTE-Positioning>` → row 1
- Last `<BTE-Positioning>` → row N-1 (bottom of rotor)

This order is preserved exactly in `RotorSpec`:
```java
public record RotorSpec(
    int id,
    char[] rightColumn,  // top→bottom order from XML
    char[] leftColumn,   // top→bottom order from XML
    int notch
)
```

### Reflector Wiring
Reflector pairs are processed in **XML order** and stored in a mapping array:
```java
int[] mapping = new int[alphabetSize];
for (each <BTE-Reflect> pair in XML order) {
    mapping[inputIndex] = outputIndex;
    mapping[outputIndex] = inputIndex;
}
```

**Why This Matters**:
- The mechanical model depends on exact wire positioning
- Reordering would produce incorrect encryption results
- The loader is the authoritative source for wiring order

## Error Handling

All validation errors throw `EnigmaLoadingException` with:
- **What's wrong**: Clear description of the problem
- **Where**: Location in XML (rotor ID, reflector ID, line number if available)
- **How to fix**: Actionable guidance

**Example Error Messages**:
```
"Alphabet validation failed: Alphabet size must be even. Found 7 characters. 
Fix: Add or remove one character to make the length even."

"Rotor validation failed: Rotor ID 3 has non-bijective right column. 
Character 'A' appears 2 times. Fix: Ensure each character appears exactly once."

"Reflector validation failed: Reflector 'II' is missing from specification. 
Reflectors must be contiguous starting from 'I'. Fix: Add reflector 'II' or remove reflectors after 'I'."
```

## Output: MachineSpec

After successful validation, the loader produces a `MachineSpec`:
```java
public record MachineSpec(
    String alphabet,                  // validated alphabet string
    int rotorsInUse,                  // number of rotors in configuration
    List<RotorSpec> rotors,           // validated rotor specifications
    List<ReflectorSpec> reflectors    // validated reflector specifications
)
```

This spec is then used by:
- Engine: For validation (rotor/reflector existence checks)
- CodeFactory: For building Code instances
- Console: For displaying available options

## Usage Example

```java
Loader loader = new LoaderXml(3); // 3 rotors in use

try {
    MachineSpec spec = loader.loadSpecs("enigma.xml");
    
    // Spec is valid and ready to use
    System.out.println("Alphabet: " + spec.alphabet());
    System.out.println("Rotors: " + spec.rotors().size());
    System.out.println("Reflectors: " + spec.reflectors().size());
    
} catch (EnigmaLoadingException e) {
    System.err.println("Failed to load: " + e.getMessage());
}
```

## Validation vs Runtime Separation

The loader performs **structural validation** at load time. The engine performs **semantic validation** at configuration time.

### Loader Validates (Structure):
- File format and XML schema
- Alphabet properties
- Rotor ID sequence and wiring
- Reflector ID sequence and wiring
- All characters are ASCII

### Engine Validates (Semantics):
- Selected rotor IDs exist in loaded spec
- Selected reflector ID exists in loaded spec
- Position characters are in alphabet
- Input characters are in alphabet
- Configuration matches rotors-in-use count

**Rationale**: Loader ensures XML is structurally valid. Engine ensures runtime choices are semantically valid.

## JAXB Generated Classes

The loader uses JAXB-generated classes from XML schema:
- `CTEEnigma`: Root element
- `CTEMachine`: Machine element
- `CTEAlphabet`: Alphabet element
- `CTERotors`: Rotors container
- `CTERotor`: Individual rotor
- `CTEReflectors`: Reflectors container
- `CTEReflector`: Individual reflector
- `CTEPositioning`: Rotor wire row

These classes are generated by JAXB tooling and should not be manually edited.

## XML File Extension Requirement

The loader requires files to end with `.xml` extension. This is a defensive check to avoid attempting to parse non-XML files.

**Valid**: `enigma.xml`, `machine.xml`, `config.XML`  
**Invalid**: `enigma.txt`, `machine`, `config.json`

## Thread Safety

LoaderXml is **stateless** and **thread-safe**. Each `loadSpecs()` call is independent and can be executed concurrently by different threads.

## Related Documentation

- [XML Validation Review](../docs/XML_VALIDATION_REVIEW.md)
- [Validation Layer Organization](../docs/VALIDATION_LAYER_ORGANIZATION.md)
- Main [README.md](../README.md)
