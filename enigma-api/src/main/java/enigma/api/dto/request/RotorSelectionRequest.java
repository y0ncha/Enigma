package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RotorSelectionRequest(
        @NotNull(message = "rotorNumber is required")
        Integer rotorNumber,

        @NotBlank(message = "rotorPosition is required")
        @Size(min = 1, max = 1, message = "rotorPosition must be a single character")
        String rotorPosition
) {
}
