# Snapshot System Architecture

## Overview

The Enigma snapshot system provides complete save/load functionality for preserving and restoring the exact state of the engine. This enables users to save their work mid-session and resume later without losing configuration, rotor positions, or processing history.

## Design Goals

1. **Completeness**: Capture all information needed to restore exact engine state
2. **Correctness**: Loaded snapshots behave identically to original engine
3. **Robustness**: Graceful handling of corrupted or invalid snapshot files
4. **Usability**: Simple API with clear error messages
5. **Maintainability**: Clean separation of concerns and well-documented code

## Architecture Components

### 1. Core Data Structures

#### EngineSnapshot
- **Location**: `enigma-engine/src/enigma/engine/snapshot/EngineSnapshot.java`
- **Purpose**: Root container for complete engine state
- **Contents**:
  - `MachineSpec`: Machine definition (alphabet, rotors, reflectors)
  - `MachineState`: Runtime state (counters, configurations)
  - `MachineHistory`: Processing history grouped by configuration

#### MachineState
- **Location**: `enigma-shared/src/enigma/shared/state/MachineState.java`
- **Purpose**: Snapshot of engine runtime state
- **Fields**:
  - `numOfRotors`: Total rotors defined in spec
  - `numOfReflectors`: Total reflectors defined in spec
  - `stringsProcessed`: Count of processed messages
  - `ogCodeState`: Original code configuration (reset target)
  - `curCodeState`: Current code configuration (after processing)

#### CodeState
- **Location**: `enigma-shared/src/enigma/shared/state/CodeState.java`
- **Purpose**: Complete code configuration with positions
- **Fields**:
  - `rotorIds`: Rotor identifiers (left→right)
  - `positions`: Current rotor window characters
  - `notchDist`: Distance to next notch for each rotor
  - `reflectorId`: Reflector identifier
  - `plugboard`: Plugboard pairs (empty if none)

#### MachineHistory
- **Location**: `enigma-engine/src/enigma/engine/history/MachineHistory.java`
- **Purpose**: Track all processed messages grouped by configuration
- **Structure**: `Map<String, List<MessageRecord>>`
  - Key: Compact string representation of CodeState
  - Value: List of messages processed under that configuration

### 2. Persistence Layer

#### EngineSnapshotJson
- **Location**: `enigma-engine/src/enigma/engine/snapshot/EngineSnapshotJson.java`
- **Purpose**: Handle JSON serialization/deserialization
- **Features**:
  - Pretty-printed JSON output
  - Standard `.enigma.json` file extension
  - Comprehensive error handling and validation
  - User-friendly error messages

### 3. Engine Integration

#### Save Flow
```
Engine.saveSnapshot(basePath)
  ↓
1. Capture current MachineState
   - Get spec from engine
   - Get ogCodeState (stored at configuration)
   - Get curCodeState from machine.getCodeState()
   - Get stringsProcessed counter
   - Get history
  ↓
2. Create EngineSnapshot record
   - Bundle spec, state, and history
  ↓
3. Serialize to JSON
   - Use EngineSnapshotJson.save()
   - Write to <basePath>.enigma.json
```

#### Load Flow
```
Engine.loadSnapshot(basePath)
  ↓
1. Deserialize JSON
   - Read from <basePath>.enigma.json
   - Parse into EngineSnapshot
   - Validate basic structure
  ↓
2. Restore engine state
   - Replace spec
   - Restore history
   - Restore counters
  ↓
3. Restore machine configuration
   - Configure with ORIGINAL CodeState (reset target)
   - Manually advance rotors to CURRENT positions
   - Preserve ability to reset to original
  ↓
4. Ready for use
   - Engine can process new messages
   - Reset returns to original positions
   - History is complete
```

## Key Design Decisions

### Original vs Current Positions

The snapshot system maintains both original and current rotor positions to ensure correct reset behavior:

- **Original (`ogCodeState`)**: Configuration set by user, used as reset target
- **Current (`curCodeState`)**: Actual rotor positions after processing

**Why Both?**
- Reset must return to original positions (user expectation)
- Processing advances rotors (mechanical behavior)
- Both states needed to preserve complete functionality

**Implementation**:
1. Load configures machine with original positions
2. `Machine.setPositions()` advances to current positions
3. `Machine.reset()` returns to original positions

### History Grouping

Messages are grouped by original code configuration using the compact string format:
```
<1,2,3><O(4),D(2),X(11)><I>
```

**Benefits**:
- Clear visual representation of configuration
- Groups all work done from a starting point
- Survives JSON serialization (string key)
- Human-readable in saved files

### Validation Strategy

Snapshot loading validates at multiple layers:

1. **File System**: File exists, readable
2. **JSON Syntax**: Valid JSON structure
3. **Required Fields**: Spec present, not null
4. **Semantic**: Spec → CodeConfig → Machine validation chain

**Error Messages**: Each layer provides specific, actionable feedback

### State Restoration Order

Critical that restoration happens in correct order:

1. Replace spec (defines what's possible)
2. Restore history (independent of configuration)
3. Restore counters (simple state)
4. Configure machine with original (sets reset target)
5. Advance to current positions (preserves processing state)

## Testing Strategy

### Unit Tests

1. **Round-Trip Tests** (`SnapshotRoundTripTester`):
   - Scenario A: No messages processed
   - Scenario B: After single process
   - Scenario C: Many process operations
   - Scenario D: Before code selection
   - Reset after load
   - Multiple configurations

2. **Validation Tests** (`SnapshotValidationTester`):
   - Missing file
   - Malformed JSON
   - Missing spec
   - Missing state
   - Empty snapshot
   - Invalid code state

### Test Coverage

- ✅ State preservation across save/load
- ✅ History preservation
- ✅ Reset functionality after load
- ✅ Error handling for invalid files
- ✅ Multiple configurations in history
- ✅ Unconfigured machine snapshots
- ✅ Position advancement preservation

## File Format

### Structure
```json
{
  "spec": {
    "alphabet": { "letters": "ABC..." },
    "rotorsById": { "1": {...}, "2": {...} },
    "reflectorsById": { "I": {...} },
    "rotorsInUse": 3
  },
  "machineState": {
    "numOfRotors": 3,
    "numOfReflectors": 1,
    "stringsProcessed": 5,
    "ogCodeState": {
      "rotorIds": [1, 2, 3],
      "positions": "ODX",
      "notchDist": [4, 2, 11],
      "reflectorId": "I",
      "plugboard": ""
    },
    "curCodeState": {
      "rotorIds": [1, 2, 3],
      "positions": "ODE",
      "notchDist": [4, 2, 6],
      "reflectorId": "I",
      "plugboard": ""
    }
  },
  "history": {
    "history": {
      "<1,2,3><O(4),D(2),X(11)><I>": [
        {
          "originalText": "HELLO",
          "processedText": "DLTBB",
          "durationNanos": 1234567
        }
      ]
    },
    "currentOriginalCode": {...}
  }
}
```

### Design Notes

- **Pretty-printed**: Human-readable for debugging
- **Self-contained**: No external references
- **Standard format**: JSON for maximum compatibility
- **Versioning**: Could add version field in future

## Extension Points

### Future Enhancements

1. **Versioning**: Add schema version for backward compatibility
2. **Compression**: Add optional gzip compression for large histories
3. **Encryption**: Add optional encryption for sensitive data
4. **Metadata**: Add timestamp, user, description fields
5. **Partial Snapshots**: Save only configuration without history

### API Extensions

1. **Snapshot comparison**: Compare two snapshots for differences
2. **Snapshot merge**: Combine histories from multiple snapshots
3. **Snapshot export**: Export to other formats (CSV, XML)
4. **Snapshot validation**: Validate without loading

## Best Practices

### For Users

1. **Naming**: Use descriptive names: `project-phase1-snapshot`
2. **Organization**: Keep snapshots in dedicated directory
3. **Backups**: Critical snapshots should be backed up
4. **Testing**: Test load immediately after save

### For Developers

1. **Validation**: Always validate input at boundaries
2. **Error Messages**: Provide actionable feedback
3. **Testing**: Add tests for new CodeState fields
4. **Documentation**: Update this doc for major changes

## Troubleshooting

### Common Issues

**Q: Snapshot fails to load with "invalid or empty"**
- Check file exists at expected path
- Verify JSON syntax is valid
- Ensure spec field is present

**Q: Reset doesn't work after loading snapshot**
- Verify original and current states are both saved
- Check that Machine.setPositions() is called
- Ensure isSnapshot flag is used correctly

**Q: History is empty after load**
- Check that history field is in JSON
- Verify history map keys are properly formatted
- Ensure currentOriginalCode is set

## References

- [Snapshot Test Suite](../src/test/enigma/engine/snapshot/)
- [EngineImpl.saveSnapshot()](../src/enigma/engine/EngineImpl.java)
- [EngineImpl.loadSnapshot()](../src/enigma/engine/EngineImpl.java)
- [EngineSnapshotJson](../src/enigma/engine/snapshot/EngineSnapshotJson.java)
