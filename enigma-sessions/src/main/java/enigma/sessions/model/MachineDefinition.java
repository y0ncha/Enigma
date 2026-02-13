package enigma.sessions.model;

import java.time.Instant;

public record MachineDefinition(
        String machineName,
        String xmlPath,
        Instant loadedAt
) {
}
