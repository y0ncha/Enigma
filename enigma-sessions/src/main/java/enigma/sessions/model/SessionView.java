package enigma.sessions.model;

import java.time.Instant;
import java.util.UUID;

public record SessionView(
        UUID sessionId,
        String machineName,
        SessionStatus status,
        Instant openedAt,
        Instant closedAt
) {
}
