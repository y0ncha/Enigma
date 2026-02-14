package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionApiRequest(
        @NotBlank(message = "machine is required")
        String machine
) {
}
