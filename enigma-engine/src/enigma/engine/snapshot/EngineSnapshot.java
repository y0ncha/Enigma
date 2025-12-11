package enigma.engine.snapshot;

import enigma.engine.history.MachineHistory;
import enigma.shared.spec.MachineSpec;
import enigma.shared.state.MachineState;

/**
 * Immutable snapshot of complete engine state for save/load functionality.
 *
 * <p><b>Module:</b> enigma-engine (snapshot)</p>
 *
 * <h2>Purpose</h2>
 * <p>EngineSnapshot captures the complete logical state of the Enigma engine at a
 * specific moment in time. This enables saving and restoring the exact machine
 * configuration, rotor positions, message history, and processing statistics without
 * requiring the original XML specification file.</p>
 *
 * <p>Used by the bonus feature: save & load a concrete machine state
 * to/from a JSON file (instead of reloading from XML).</p>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li><b>spec:</b> Complete {@link MachineSpec} defining the machine
 *       <ul>
 *         <li>Alphabet</li>
 *         <li>All available rotor definitions (by ID)</li>
 *         <li>All available reflector definitions (by ID)</li>
 *         <li>Number of rotors in use</li>
 *       </ul>
 *   </li>
 *   <li><b>machineState:</b> Runtime state snapshot ({@link MachineState})
 *       <ul>
 *         <li>Rotor and reflector counts</li>
 *         <li>Number of messages processed</li>
 *         <li>Original code configuration (for reset target)</li>
 *         <li>Current code configuration (with advanced rotor positions)</li>
 *       </ul>
 *   </li>
 *   <li><b>history:</b> Complete {@link MachineHistory}
 *       <ul>
 *         <li>All processed messages grouped by original code</li>
 *         <li>Input/output pairs with processing duration</li>
 *         <li>Configuration history</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li><b>Completeness:</b> Captures all information needed to restore exact state</li>
 *   <li><b>Immutability:</b> Record type ensures snapshot cannot be modified</li>
 *   <li><b>Self-contained:</b> No external dependencies beyond the snapshot file</li>
 *   <li><b>JSON-friendly:</b> All fields serialize cleanly with Gson</li>
 * </ul>
 *
 * <h2>Guarantees</h2>
 * <p>A properly saved and loaded snapshot ensures:</p>
 * <ul>
 *   <li>Machine specification is identical (alphabet, rotors, reflectors)</li>
 *   <li>Original code configuration is preserved (for reset operations)</li>
 *   <li>Current rotor positions are restored exactly</li>
 *   <li>Processing history is complete and accurate</li>
 *   <li>Message counter reflects actual processing count</li>
 *   <li>Reset functionality works correctly after load</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Save current engine state
 * Engine engine = new EngineImpl();
 * engine.loadMachine("enigma.xml");
 * engine.configManual(config);
 * engine.process("SECRET");
 * engine.saveSnapshot("/path/to/snapshot");
 *
 * // Load saved state in new engine
 * Engine newEngine = new EngineImpl();
 * newEngine.loadSnapshot("/path/to/snapshot");
 * // Engine is now in exact same state, including rotor positions
 * 
 * // Reset works correctly
 * newEngine.reset();  // Returns to original positions
 * </pre>
 *
 * <h2>File Format</h2>
 * <p>Snapshots are saved as pretty-printed JSON with the extension
 * {@code .enigma.json}. See {@link EngineSnapshotJson} for serialization details.</p>
 *
 * @param spec Complete machine specification (alphabet, rotors, reflectors)
 * @param machineState Runtime state (counters, original/current configs)
 * @param history Complete processing history grouped by configuration
 * @see EngineSnapshotJson
 * @see MachineState
 * @see MachineHistory
 * @since 1.0
 */
public record EngineSnapshot(
        MachineSpec spec,
        MachineState machineState,
        MachineHistory history

) {
}
