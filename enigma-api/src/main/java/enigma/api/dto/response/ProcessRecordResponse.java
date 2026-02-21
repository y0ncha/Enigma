package enigma.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProcessRecordResponse(
        UUID id,
        UUID sessionId,
        String machineName,
        String code,
        String inputText,
        String outputText,
        long durationNanos,
        Instant processedAt
) {
}
