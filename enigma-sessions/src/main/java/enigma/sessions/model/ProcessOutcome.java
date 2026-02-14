package enigma.sessions.model;

import enigma.shared.state.MachineState;

import java.util.UUID;

public record ProcessOutcome(
        UUID sessionId,
        String machineName,
        String input,
        String output,
        long durationMillis,
        MachineState machineState
) {
}
