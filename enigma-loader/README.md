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

## Validation Rules

### XML File Validation
- ✅ File must exist
- ✅ File extension must be `.xml`
- ✅ XML must be well-formed
- ✅ XML must match JAXB schema

### Alphabet Validation
- ✅ Non-empty
- ✅ **Even length** (required for reflector pairs)
- ✅ **Unique characters** (no duplicates)
- ✅ ASCII characters only

**Example Valid Alphabets**:
- "ABCDEFGHIJKLMNOPQRSTUVWXYZ" (26 chars, even ✓)
- "ABCDEF" (6 chars, even ✓)

**Example Invalid Alphabets**:
- "ABCDEFG" (7 chars, odd ✗)
- "ABBA" (duplicate 'A' and 'B' ✗)

### Rotor Validation

#### Rotor IDs
- ✅ Must form **contiguous sequence starting at 1**
- ✅ No gaps allowed
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

### Reflector Validation

#### Reflector IDs
- ✅ Must be **Roman numerals**: I, II, III, IV, V
- ✅ Must be **contiguous starting from I**
- ✅ Must be **unique**

**Valid**: I, II, III  
**Invalid**: I, III (gap at II)  
**Invalid**: II, III, IV (must start at I)  
**Invalid**: I, I, II (duplicate I)

**Rationale**: Roman numerals are historical Enigma convention. The loader enforces this format so the engine doesn't need to parse Roman numerals.

#### Reflector Mappings (Symmetry)
Reflector mappings are defined as pairs in XML:
```xml
<BTE-Reflect>
    <Input>A</Input>
    <Output>B</Output>
</BTE-Reflect>
```

Requirements:
- ✅ **Symmetric**: If A→B, then B→A (automatically enforced by loader construction)
- ✅ **No self-mapping**: Character cannot map to itself (typically required)
- ✅ **Complete coverage**: All alphabet characters must be paired
- ✅ **Even number of pairs**: alphabetSize / 2 pairs required

**Symmetry Enforcement**:
The loader enforces symmetry **by construction**:
```java
mapping[inputIndex] = outputIndex;
mapping[outputIndex] = inputIndex;
```
This makes asymmetric mappings impossible.

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
