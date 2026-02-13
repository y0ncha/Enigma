package enigma.api.dto.response;

import enigma.sessions.model.SessionStatus;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        String machineName,
        SessionStatus status,
        Instant openedAt,
        Instant closedAt
) {
}
