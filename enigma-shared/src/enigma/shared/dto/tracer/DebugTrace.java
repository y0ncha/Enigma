package enigma.shared.dto.tracer;

import java.util.List;

/**
 * Container for full debug output from processing a string through the machine.
 *
 * <p><b>Module:</b> enigma-shared (DTOs)</p>
 *
 * <p>Bundles the final encrypted output string with detailed per-character
 * signal traces. Each {@link SignalTrace} contains the complete encryption
 * path for one character including rotor stepping, forward pass, reflector,
 * and backward pass.</p>
 *
 * <h2>Usage</h2>
 * <p>Returned by {@link enigma.engine.Engine#process(String)} to provide
 * both the result and detailed debugging information for analysis or display.</p>
 *
 * @param output final processed output string
 * @param signalTraces per-character signal traces (one per input character)
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
