package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ManualConfigRequest(
        @NotNull(message = "sessionId is required")
        UUID sessionId,

        @NotEmpty(message = "rotorIds must contain values")
        List<Integer> rotorIds,

        @NotBlank(message = "positions is required")
        String positions,

        @NotBlank(message = "reflectorId is required")
        String reflectorId,

        String plugboard
) {
}
