package enigma.sessions.model;

import java.time.Instant;
import java.util.UUID;

public record ProcessRecordView(
        UUID id,
        UUID sessionId,
        String machineName,
        String code,
        String inputText,
        String outputText,
        long durationMillis,
        Instant processedAt
) {
}
