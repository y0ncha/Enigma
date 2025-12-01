package enigma.shared.dto.tracer;

import java.util.List;

public record DebugTrace(
        String output,                  // final processed string
        List<SignalTrace> signalTraces  // one trace per input character
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== DebugTrace ===\n");
        sb.append("Final output: ").append(output).append("\n\n");

        for (int i = 0; i < signalTraces.size(); i++) {
            sb.append("----- Character #").append(i + 1).append(" -----\n");
            sb.append(signalTraces.get(i).toString()).append("\n");
        }

        return sb.toString();
    }
}
