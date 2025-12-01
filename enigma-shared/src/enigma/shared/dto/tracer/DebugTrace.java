package enigma.shared.dto.tracer;

import java.util.List;

public record DebugTrace(
        String output,                  // final processed string
        List<SignalTrace> signalTraces  // one trace per input character
)
{}
