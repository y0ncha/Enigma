package enigma.engine.history;

import enigma.shared.dto.record.MessageRecord;
import enigma.shared.state.CodeState;
import java.util.*;

public final class MachineHistory {

    // Original code -> all message runs under that code
    private final Map<CodeState, List<MessageRecord>> history = new LinkedHashMap<>();

    // The original code currently in effect
    private CodeState currentOriginalCode;

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

    public void recordMessage(String input, String output, long durationNanos) {
        if (currentOriginalCode == null) {
            throw new IllegalStateException(
                    "Cannot record message: No original code has been configured yet.");
        }

        // Defensive — should never happen if recordConfig() is correct
        List<MessageRecord> messages = history.computeIfAbsent(currentOriginalCode, k -> new ArrayList<>());

        messages.add(new MessageRecord(input, output, durationNanos));
    }

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