# Console Module Tests

This directory contains test coverage for the `enigma-console` module.

## Test Structure

```
test/enigma/console/
├── helper/
│   └── InputParsersTester.java      # Tests for InputParsers utility class
├── ConsoleCommandTester.java        # Tests for ConsoleCommand enum
└── ConsoleImplTester.java           # Integration tests for ConsoleImpl
```

## Test Coverage

### ✓ Helper Classes (Standalone - No Dependencies)

#### InputParsersTester
Tests the input parsing utilities used throughout the console:
- **parseRotorIds()**: Parsing comma-separated rotor IDs
  - Valid formats: "1,2,3", "10, 20, 30", "42"
  - Error handling: non-numeric values, invalid input
  - Edge cases: empty parts, whitespace handling
  
- **toRoman()**: Integer to Roman numeral conversion
  - Valid conversions: 1→I, 2→II, 3→III, 4→IV, 5→V
  - Out of range: 6→?6, 10→?10
  
- **buildInitialPositions()**: Position string to index list
  - Valid inputs: "ABC"→[2,1,0], "CCC"→[2,2,2]
  - Case-insensitive: "abc"→[2,1,0]
  - Error handling: non-letter characters, special characters

**Status**: ✓ All tests passing

#### ConsoleCommandTester
Tests the command enumeration and parsing:
- **fromId()**: Command lookup by numeric ID
  - Valid IDs (1-8): Correct command mapping
  - Invalid IDs (0, 9, -1, 100): Returns null
  
- **Command Properties**: 
  - Valid ID range (1-8)
  - Non-empty descriptions
  - Unique IDs across all commands

**Status**: ✓ All tests passing

### ✓ Integration Tests (Requires Engine Dependency)

#### ConsoleImplTester
Tests the main console interaction flow:
- **Invalid Input Handling**: Empty input, non-numeric input, out-of-range numbers
- **Command State Validation**: State-dependent command enablement
- **Exit Command**: Clean shutdown

**Status**: ⚠ Requires Java 21 and full project compilation

## Running Tests

### Standalone Tests (No Dependencies)
These tests can be run with just the console helper classes compiled:

```bash
# Compile the helper classes
javac -d out enigma-console/src/enigma/console/helper/InputParsers.java
javac -d out enigma-console/src/enigma/console/ConsoleCommand.java

# Compile the tests
javac -d out -cp out enigma-console/src/test/enigma/console/helper/InputParsersTester.java
javac -d out -cp out enigma-console/src/test/enigma/console/ConsoleCommandTester.java

# Run tests
java -cp out test.enigma.console.helper.InputParsersTester
java -cp out test.enigma.console.ConsoleCommandTester
```

Expected output: All tests should pass with "Status: ALL GOOD ✔✔✔"

### Integration Tests (Requires Full Build)
The ConsoleImplTester requires the Engine and all dependencies:

```bash
# Requires Java 21+ and all modules compiled
javac --release 21 -d out $(find enigma-*/src/enigma -name "*.java")
javac -d out -cp out enigma-console/src/test/enigma/console/ConsoleImplTester.java
java -cp out test.enigma.console.ConsoleImplTester
```

## Test Results Summary

| Test Class | Tests | Passed | Failed | Status |
|------------|-------|--------|--------|--------|
| InputParsersTester | 5 | 5 | 0 | ✔ ALL GOOD |
| ConsoleCommandTester | 3 | 3 | 0 | ✔ ALL GOOD |
| ConsoleImplTester | 3 | - | - | ⚠ Requires Java 21 |

## Test Philosophy

These tests follow the manual tester pattern used in other modules (e.g., `enigma-engine`):
- Manual testers with `main()` methods for easy execution
- Clear, human-readable output with ✔/✘ indicators
- Comprehensive test summaries
- No external test framework dependencies
- Consistent with project testing conventions

## Future Enhancements

When the project is upgraded to Java 21 or the environment supports it:
1. Complete ConsoleImplTester integration tests
2. Add tests for specific command handlers (load XML, set code, process input)
3. Add tests for edge cases in the full interaction flow
4. Consider parameterized test data files for comprehensive coverage
