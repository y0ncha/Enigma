package enigma.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ManualConfigApiRequest(
        @NotBlank(message = "sessionID is required")
        String sessionID,

        @NotEmpty(message = "rotors must contain values")
        List<@Valid RotorSelectionRequest> rotors,

        @NotBlank(message = "reflector is required")
        String reflector,

        List<@Valid PlugConnectionRequest> plugs
) {
}
