package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProcessRequest(
        @NotNull(message = "sessionId is required")
        UUID sessionId,

        @NotBlank(message = "input is required")
        String input
) {
}
