package enigma.shared.dto.tracer;

import java.util.List;

/**
 * Container for full debug output from processing a string through the machine.
 *
 * <p>Contains the final output string and a per-character trace of the signal
 * path through rotors and reflector.</p>
 *
 * @param output final processed output string
 * @param signalTraces per-character signal traces
 * @since 1.0
 */
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
