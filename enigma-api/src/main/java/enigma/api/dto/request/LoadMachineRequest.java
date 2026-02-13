package enigma.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoadMachineRequest(
        @NotBlank(message = "xmlPath is required")
        String xmlPath
) {
}
