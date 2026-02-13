package enigma.sessions.service.impl;

import enigma.engine.EngineImpl;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ConflictException;
import enigma.sessions.exception.ResourceNotFoundException;
import enigma.sessions.model.MachineDefinition;
import enigma.sessions.model.SessionRuntime;
import enigma.sessions.model.SessionStatus;
import enigma.sessions.model.SessionView;
import enigma.sessions.service.MachineCatalogService;
import enigma.sessions.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionServiceImpl implements SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionServiceImpl.class);

    private final MachineCatalogService machineCatalogService;
    private final Map<UUID, SessionRuntime> runtimeBySessionId;
    private final Map<UUID, SessionView> sessionById;

    public SessionServiceImpl(MachineCatalogService machineCatalogService) {
        this.machineCatalogService = machineCatalogService;
        this.runtimeBySessionId = new ConcurrentHashMap<>();
        this.sessionById = new ConcurrentHashMap<>();
    }

    @Override
    @Transactional
    public SessionView openSession(String machineName) {
        log.info("Opening session for machine={}", machineName);
        MachineDefinition machineDefinition = machineCatalogService.resolveMachine(machineName);

        if (machineDefinition.xmlPath() == null || machineDefinition.xmlPath().isBlank()) {
            throw new ConflictException("Machine source XML is unavailable for '" + machineDefinition.machineName()
                    + "'. Reload it before opening a session.");
        }

        EngineImpl engine = new EngineImpl();
        try {
            engine.loadMachine(machineDefinition.xmlPath());
        }
        catch (Exception e) {
            log.error("Failed to initialize session for machine={}", machineDefinition.machineName(), e);
            throw new ApiValidationException("Unable to initialize session for machine '"
                    + machineDefinition.machineName() + "': " + e.getMessage(), e);
        }

        UUID sessionId = UUID.randomUUID();
        Instant openedAt = Instant.now();
        SessionView opened = new SessionView(
                sessionId,
                machineDefinition.machineName(),
                SessionStatus.OPEN,
                openedAt,
                null
        );

        SessionRuntime runtime = new SessionRuntime(
                sessionId,
                machineDefinition.machineId(),
                machineDefinition.machineName(),
                machineDefinition.xmlPath(),
                engine,
                openedAt
        );

        runtimeBySessionId.put(sessionId, runtime);
        sessionById.put(sessionId, opened);
        log.info("Session opened successfully sessionId={} machine={}", sessionId, machineDefinition.machineName());
        return opened;
    }

    @Override
    @Transactional
    public SessionView closeSession(UUID sessionId) {
        if (sessionId == null) {
            throw new ApiValidationException("sessionId must be provided");
        }
        log.info("Closing session sessionId={}", sessionId);

        SessionView existing = sessionById.get(sessionId);
        if (existing == null) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        if (existing.status() == SessionStatus.CLOSED) {
            throw new ConflictException("Session is already closed: " + sessionId);
        }

        Instant closedAt = Instant.now();
        SessionView closed = new SessionView(
                existing.sessionId(),
                existing.machineName(),
                SessionStatus.CLOSED,
                existing.openedAt(),
                closedAt
        );
        sessionById.put(sessionId, closed);

        SessionRuntime runtime = runtimeBySessionId.remove(sessionId);
        if (runtime != null) {
            runtime.markClosed(closedAt);
        }

        log.info("Session closed sessionId={}", sessionId);
        return closed;
    }

    @Override
    @Transactional(readOnly = true)
    public SessionView getSession(UUID sessionId) {
        if (sessionId == null) {
            throw new ApiValidationException("sessionId must be provided");
        }

        SessionView session = sessionById.get(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }
        return session;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionView> listSessions() {
        List<SessionView> result = new ArrayList<>(sessionById.values());
        result.sort(Comparator.comparing(SessionView::openedAt));
        return List.copyOf(result);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionRuntime resolveOpenRuntime(UUID sessionId) {
        if (sessionId == null) {
            throw new ApiValidationException("sessionId must be provided");
        }

        SessionRuntime runtime = runtimeBySessionId.get(sessionId);
        if (runtime == null) {
            SessionView persisted = sessionById.get(sessionId);
            if (persisted == null) {
                throw new ResourceNotFoundException("Session not found: " + sessionId);
            }
            if (persisted.status() == SessionStatus.CLOSED) {
                throw new ConflictException("Session is closed: " + sessionId);
            }
            throw new ConflictException("Session runtime is unavailable. Please open a new session.");
        }

        if (runtime.status() != SessionStatus.OPEN) {
            throw new ConflictException("Session is closed: " + sessionId);
        }

        return runtime;
    }
}
