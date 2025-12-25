package enigma.engine.snapshot;

import enigma.engine.history.MachineHistory;
import enigma.shared.spec.MachineSpec;
import enigma.shared.state.MachineState;

/**
 * Serializable snapshot of the current engine state.
 *
 * <p>Used by the bonus feature: save & load a concrete machine state
 * to/from a JSON file (instead of reloading from XML).</p>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li><b>spec:</b> Loaded {@link MachineSpec} (alphabet, rotors, reflectors, rotorsInUse)</li>
 *   <li><b>machineState:</b> Snapshot of runtime state
 *       ({@link MachineState} – rotors/reflectors count, stringsProcessed,
 *       original & current code state)</li>
 *   <li><b>history:</b> Full {@link MachineHistory} (all configurations and
 *       processed messages + statistics)</li>
 * </ul>
 *
 * <p>All fields are immutable and Gson-friendly.</p>
 */
public record EngineSnapshot(
        MachineSpec spec,
        MachineState machineState,
        MachineHistory history

) {
}
