package enigma.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConfigEventResponse(
        Long id,
        UUID sessionId,
        String machineName,
        String action,
        String payload,
        Instant createdAt
) {
}
