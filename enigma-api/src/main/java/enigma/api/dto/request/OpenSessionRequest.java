package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OpenSessionRequest(
        @NotBlank(message = "machineName is required")
        String machineName
) {
}
