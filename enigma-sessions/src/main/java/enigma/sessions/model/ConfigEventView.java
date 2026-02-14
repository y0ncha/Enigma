package enigma.sessions.model;

import java.time.Instant;
import java.util.UUID;

public record ConfigEventView(
        Long id,
        UUID sessionId,
        String machineName,
        String action,
        String payload,
        Instant createdAt
) {
}
