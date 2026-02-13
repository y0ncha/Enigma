package enigma.sessions.service;

import enigma.sessions.model.ProcessOutcome;

import java.util.UUID;

public interface ProcessingService {

    ProcessOutcome process(UUID sessionId, String input);
}
