package enigma.sessions.service;

import enigma.sessions.model.SessionRuntime;
import enigma.sessions.model.SessionView;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    SessionView openSession(String machineName);

    SessionView closeSession(UUID sessionId);

    SessionView getSession(UUID sessionId);

    List<SessionView> listSessions();

    SessionRuntime resolveOpenRuntime(UUID sessionId);

    void clearSessions();
}
