package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlugConnectionRequest(
        @NotBlank(message = "plug1 is required")
        @Size(min = 1, max = 1, message = "plug1 must be a single character")
        String plug1,

        @NotBlank(message = "plug2 is required")
        @Size(min = 1, max = 1, message = "plug2 must be a single character")
        String plug2
) {
}
