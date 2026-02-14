package enigma.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SessionCommandRequest(
        @NotNull(message = "sessionId is required")
        UUID sessionId
) {
}
