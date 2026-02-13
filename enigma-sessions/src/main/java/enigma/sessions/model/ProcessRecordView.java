package enigma.sessions.model;

import java.time.Instant;
import java.util.UUID;

public record ProcessRecordView(
        Long id,
        UUID sessionId,
        String machineName,
        String inputText,
        String outputText,
        long durationNanos,
        Instant processedAt
) {
}
