package enigma.sessions.service;

import enigma.shared.dto.config.CodeConfig;
import enigma.shared.state.MachineState;

import java.util.UUID;

public interface ConfigurationService {

    MachineState currentState(UUID sessionId);

    MachineState configureManual(UUID sessionId, CodeConfig config);

    MachineState configureRandom(UUID sessionId);

    MachineState reset(UUID sessionId);
}
