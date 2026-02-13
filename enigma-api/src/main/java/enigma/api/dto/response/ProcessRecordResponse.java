package enigma.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProcessRecordResponse(
        Long id,
        UUID sessionId,
        String machineName,
        String inputText,
        String outputText,
        long durationNanos,
        Instant processedAt
) {
}
