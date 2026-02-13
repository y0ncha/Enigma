package enigma.api.dto.response;

import java.time.Instant;

public record MachineResponse(
        String machineName,
        String xmlPath,
        Instant loadedAt
) {
}
