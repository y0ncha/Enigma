package enigma.api.dto.response;

import java.util.UUID;

public record ProcessResponse(
        UUID sessionId,
        String machineName,
        String input,
        String output,
        long durationNanos,
        MachineStateResponse machineState
) {
}
