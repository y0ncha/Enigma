package enigma.shared.state;


/**
 * Comprehensive snapshot of engine-visible machine state.
 *
 * <p><b>Module:</b> enigma-shared (state snapshots)</p>
 *
 * <h2>Purpose</h2>
 * <p>MachineState bundles all important state information from the engine into
 * a single immutable snapshot. This is useful for:</p>
 * <ul>
 *   <li>Displaying complete machine status</li>
 *   <li>Passing state to UI/console for rendering</li>
 *   <li>Testing and verification</li>
 *   <li>Serialization/logging (future)</li>
 * </ul>
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li><b>numOfRotors:</b> Total number of rotor specifications loaded
 *       <ul>
 *         <li>From XML, not necessarily all in use</li>
 *         <li>Example: 5 rotors defined, 3 in use</li>
 *       </ul>
 *   </li>
 *   <li><b>numOfReflectors:</b> Total number of reflector specifications loaded
 *       <ul>
 *         <li>From XML, typically 2-5 reflectors</li>
 *       </ul>
 *   </li>
 *   <li><b>stringsProcessed:</b> Count of messages processed
 *       <ul>
 *         <li>Point-in-time snapshot</li>
 *         <li>Increments with each engine.process() call</li>
 *       </ul>
 *   </li>
 *   <li><b>ogCodeState:</b> Original code state (captured at configuration)
 *       <ul>
 *         <li>Contains initial positions</li>
 *         <li>Used for reset target</li>
 *         <li>Null if not configured</li>
 *       </ul>
 *   </li>
 *   <li><b>curCodeState:</b> Current code state (current positions)
 *       <ul>
 *         <li>Contains positions after processing</li>
 *         <li>Changes as characters processed</li>
 *         <li>Null if not configured</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Original vs Current Code</h2>
 * <p>The state includes both original and current code configurations:</p>
 * <ul>
 *   <li><b>Original:</b> How machine was configured (reset target)</li>
 *   <li><b>Current:</b> Current state after processing (positions may differ)</li>
 * </ul>
 *
 * <p><b>Example</b>:</p>
 * <pre>
 * Configure: ogCodeState = <1,2,3><O(5),D(12),X(3)><I>
 * Process "A": curCodeState = <1,2,3><O(5),D(12),Y(2)><I>
 * Process "B": curCodeState = <1,2,3><O(5),D(12),Z(1)><I>
 * Reset: curCodeState returns to ogCodeState
 * </pre>
 *
 * <h2>Immutability and Thread Safety</h2>
 * <p>As a record, MachineState is immutable. However, it is a <b>snapshot</b>
 * at a specific moment. The engine's actual state may change after this
 * snapshot is created. Use this for display and logging, not for making
 * state-dependent decisions.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Get comprehensive state from engine
 * MachineState state = engine.machineData();
 *
 * // Display to user
 * System.out.println(state);
 * // Output:
 * // Machine State
 * // ------------------------------
 * // Rotors Defined        : 5
 * // Reflectors Defined    : 2
 * // Strings Processed     : 3
 * //
 * // Original Configuration : <1,2,3><O(5),D(12),X(3)><I>
 * // Current Configuration  : <1,2,3><O(5),D(11),A(25)><I>
 * </pre>
 *
 * @param numOfRotors number of rotors defined in the loaded spec
 * @param numOfReflectors number of reflectors defined in the loaded spec
 * @param stringsProcessed snapshot of processed-strings counter
 * @param ogCodeState original code state (initial positions), null if not configured
 * @param curCodeState current code state (current positions), null if not configured
 * @since 1.0
 */
public record MachineState(
        int numOfRotors,
        int numOfReflectors,
        int stringsProcessed,
        CodeState ogCodeState,
        CodeState curCodeState
) {
    /**
     * Human-readable, tester-friendly summary of the machine’s state.
     * Outputs rotor/reflector counts, processed message count,
     * and both original + current code configurations.
     */
    @Override
    public String toString() {

        return  "Rotors Defined        : " + numOfRotors + "\n" +
                "Reflectors Defined    : " + numOfReflectors + "\n" +
                "Strings Processed     : " + stringsProcessed + "\n" +
                "Original Configuration : " +
                (ogCodeState == null || ogCodeState == CodeState.NOT_CONFIGURED ? "<not configured>" : ogCodeState.toString()) +
                "\n" +
                "Current Configuration  : " +
                (curCodeState == null || curCodeState == CodeState.NOT_CONFIGURED ? "<not configured>" : curCodeState.toString());
    }
}
