package enigma.sessions.service;

import enigma.sessions.model.HistoryView;

import java.util.UUID;

public interface HistoryQueryService {

    HistoryView bySession(UUID sessionId);

    HistoryView byMachineName(String machineName);
}
