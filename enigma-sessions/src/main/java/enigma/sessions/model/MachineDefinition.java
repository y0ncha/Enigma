package enigma.sessions.model;

import java.time.Instant;
import java.util.UUID;

public record MachineDefinition(
        UUID machineId,
        String machineName,
        String xmlPath,
        Instant loadedAt
) {
}
