package enigma.engine.history;

import enigma.shared.dto.record.MessageRecord;
import enigma.shared.state.CodeState;
import java.util.*;

/**
 * Records and organizes machine processing history by original code configuration.
 *
 * <p><b>Module:</b> enigma-engine</p>
 *
 * <h2>Purpose</h2>
 * <p>MachineHistory maintains a chronological record of all messages processed by the
 * Enigma machine, grouped by their <b>original code configuration</b>. This allows
 * viewing all encryption work performed from a particular starting configuration,
 * even though rotor positions change with each character processed.</p>
 *
 * <h2>Key Concepts</h2>
 *
 * <h3>Original Code</h3>
 * <p>The "original code" is the complete code configuration (rotor IDs, initial positions,
 * reflector ID, plugboard) captured at the moment of configuration via
 * {@code Engine.configManual()} or {@code Engine.configRandom()}.</p>
 *
 * <p><b>Example</b>: If you configure rotors [1,2,3] at positions ['O','D','X']
 * with reflector "I", this entire state is the original code. All subsequent messages
 * processed are grouped under this original code.</p>
 *
 * <h3>Message Grouping</h3>
 * <p>All messages processed after a configuration are grouped under that original code,
 * regardless of how rotor positions change during processing. This design allows:</p>
 * <ul>
 *   <li>Viewing all work done from a specific starting point</li>
 *   <li>Comparing encryption results under different configurations</li>
 *   <li>Maintaining clear audit trail of machine usage</li>
 * </ul>
 *
 * <p><b>Example Flow</b>:</p>
 * <pre>
 * 1. Configure code: [1,2,3], ['O','D','X'], "I"  ← Original Code A
 * 2. Process "HELLO" → "XYZAB"                    ← Grouped under A
 * 3. Process "WORLD" → "PQRST"                    ← Grouped under A
 * 4. Reset (positions return to O,D,X)            ← Still under A
 * 5. Process "TEST" → "ABCD"                      ← Grouped under A
 * 6. Configure new code: [3,2,1], ['A','A','A'], "II" ← Original Code B
 * 7. Process "HELLO" → "MNOPQ"                    ← Grouped under B
 * </pre>
 *
 * <h3>When History Resets</h3>
 * <p>History is cleared <b>only</b> when:</p>
 * <ul>
 *   <li>A new machine is loaded ({@code Engine.loadMachine()}) - different machine = fresh start</li>
 *   <li>Engine is terminated ({@code Engine.terminate()}) - explicit state clearing</li>
 * </ul>
 *
 * <p>History is <b>NOT</b> cleared when:</p>
 * <ul>
 *   <li>Reset is called ({@code Engine.reset()}) - positions return to original but history remains</li>
 *   <li>New code is configured - creates new group in history, old groups remain</li>
 * </ul>
 *
 * <h3>Why Map&lt;CodeState, List&lt;MessageRecord&gt;&gt;?</h3>
 * <p>Using {@code CodeState} as the key (instead of just positions or rotor IDs) ensures
 * that different configurations with the same rotor IDs but different starting positions
 * are treated as distinct original codes. This is important because:</p>
 * <ul>
 *   <li>Same rotors at different positions produce different encryption</li>
 *   <li>History should reflect the exact starting state</li>
 *   <li>Reset needs to know the precise original positions</li>
 * </ul>
 *
 * <h2>Data Structure</h2>
 * <pre>
 * history: Map&lt;CodeState, List&lt;MessageRecord&gt;&gt;
 *   ├─ CodeState(rotors=[1,2,3], positions=['O','D','X'], reflector="I", ...)
 *   │    ├─ MessageRecord("HELLO", "XYZAB", 1234567 nanos)
 *   │    ├─ MessageRecord("WORLD", "PQRST", 987654 nanos)
 *   │    └─ MessageRecord("TEST", "ABCD", 456789 nanos)
 *   └─ CodeState(rotors=[3,2,1], positions=['A','A','A'], reflector="II", ...)
 *        └─ MessageRecord("HELLO", "MNOPQ", 2345678 nanos)
 * </pre>
 *
 * <h2>Thread Safety</h2>
 * <p>This class is <b>NOT thread-safe</b>. It is designed for single-threaded use
 * within the engine. Concurrent access requires external synchronization.</p>
 *
 * @since 1.0
 */
public final class MachineHistory {

    // Original code -> all message runs under that code
    private final Map<CodeState, List<MessageRecord>> history = new LinkedHashMap<>();

    // The original code currently in effect
    private CodeState currentOriginalCode;

    /**
     * Records a new original code configuration and sets it as the current grouping key.
     *
     * <p>This method should be called whenever the engine configures the machine
     * with a new code (via {@code configManual()} or {@code configRandom()}).
     * It establishes the original code that all subsequent processed messages
     * will be grouped under.</p>
     *
     * <p><b>Effect</b>:</p>
     * <ul>
     *   <li>Sets {@code currentOriginalCode} to the provided state</li>
     *   <li>Creates an empty message list for this code if it doesn't exist</li>
     *   <li>If this code was used before, messages append to existing list</li>
     * </ul>
     *
     * <p><b>Example</b>:</p>
     * <pre>
     * // Engine configures machine
     * CodeState original = machine.getCodeState();
     * history.recordConfig(original);  // Establish grouping key
     * </pre>
     *
     * @param codeState the original code state to record (must not be null)
     * @throws IllegalArgumentException if codeState is null
     */
    public void recordConfig(CodeState codeState) {
        if (codeState == null) {
            throw new IllegalArgumentException(
                    "Cannot record configuration: codeState is null.");
        }

        // Set the currently active original code state
        currentOriginalCode = codeState;

        // Ensure this codeState has an entry in the history map
        history.computeIfAbsent(codeState, k -> new ArrayList<>());
    }

    /**
     * Records a processed message under the current original code.
     *
     * <p>This method should be called after each successful message processing
     * via {@code Engine.process()}. The message is added to the list associated
     * with the current original code (set by {@link #recordConfig(CodeState)}).</p>
     *
     * <p><b>Precondition</b>: {@code recordConfig()} must have been called at least
     * once to establish the current original code.</p>
     *
     * <p><b>Example</b>:</p>
     * <pre>
     * // Engine processes message
     * long startTime = System.nanoTime();
     * String output = machine.process(input);
     * long duration = System.nanoTime() - startTime;
     * history.recordMessage(input, output, duration);
     * </pre>
     *
     * @param input the input message (plaintext or ciphertext)
     * @param output the output message (encrypted or decrypted result)
     * @param durationNanos processing duration in nanoseconds
     * @throws IllegalStateException if no original code has been configured yet
     */
    public void recordMessage(String input, String output, long durationNanos) {
        if (currentOriginalCode == null) {
            throw new IllegalStateException(
                    "Cannot record message: No original code has been configured yet.");
        }

        // Defensive — should never happen if recordConfig() is correct
        List<MessageRecord> messages = history.computeIfAbsent(currentOriginalCode, k -> new ArrayList<>());

        messages.add(new MessageRecord(input, output, durationNanos));
    }

    /**
     * Generates a formatted string representation of the complete processing history.
     *
     * <p>The output is grouped by original code configuration, showing all messages
     * processed under each configuration. For each message, the input, output, and
     * processing duration are displayed.</p>
     *
     * <p><b>Format</b>:</p>
     * <pre>
     * === Original Code: CodeState(rotors=[1,2,3], positions=['O','D','X'], ...) ===
     *   • MessageRecord(input="HELLO", output="XYZAB", duration=1234567 nanos)
     *   • MessageRecord(input="WORLD", output="PQRST", duration=987654 nanos)
     *
     * === Original Code: CodeState(rotors=[3,2,1], positions=['A','A','A'], ...) ===
     *   • MessageRecord(input="TEST", output="ABCD", duration=456789 nanos)
     * </pre>
     *
     * <p>If no messages have been processed, returns a message indicating
     * empty history.</p>
     *
     * @return formatted history string for display
     */
    @Override
    public String toString() {
        if (history.isEmpty()) {
            return "No history available. No messages were processed.";
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<CodeState, List<MessageRecord>> entry : history.entrySet()) {
            CodeState code = entry.getKey();
            List<MessageRecord> records = entry.getValue();

            sb.append("=== Original Code: ")
                    .append(code)
                    .append(" ===\n");

            if (records.isEmpty()) {
                sb.append("  No messages processed under this configuration.\n\n");
                continue;
            }

            for (MessageRecord record : records) {
                sb.append("  • ").append(record.toString()).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}