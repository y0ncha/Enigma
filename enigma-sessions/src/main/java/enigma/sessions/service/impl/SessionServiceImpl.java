package enigma.sessions.service.impl;

import enigma.dal.entity.SessionEntity;
import enigma.dal.repository.SessionRepository;
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

import jakarta.annotation.PostConstruct;
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
    private final SessionRepository sessionRepository;
    private final Map<UUID, SessionRuntime> runtimeBySessionId;

    public SessionServiceImpl(MachineCatalogService machineCatalogService, SessionRepository sessionRepository) {
        this.machineCatalogService = machineCatalogService;
        this.sessionRepository = sessionRepository;
        this.runtimeBySessionId = new ConcurrentHashMap<>();
    }

    @PostConstruct
    @Transactional
    void closeDanglingSessions() {
        log.info("Scanning for dangling open sessions at startup");
        List<SessionEntity> allSessions = sessionRepository.findAll();
        Instant now = Instant.now();
        boolean changed = false;

        for (SessionEntity session : allSessions) {
            if (SessionStatus.OPEN.name().equals(session.getStatus())) {
                session.setStatus(SessionStatus.CLOSED.name());
                session.setClosedAt(now);
                changed = true;
            }
        }

        if (changed) {
            sessionRepository.saveAll(allSessions);
            log.info("Closed dangling sessions at startup");
        }
    }

    @Override
    @Transactional
    public SessionView openSession(String machineName) {
        log.info("Opening session for machine={}", machineName);
        MachineDefinition machineDefinition = machineCatalogService.resolveMachine(machineName);

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

        SessionEntity persisted = sessionRepository.save(new SessionEntity(
                sessionId,
                machineDefinition.machineName(),
                SessionStatus.OPEN.name(),
                openedAt,
                null
        ));

        SessionRuntime runtime = new SessionRuntime(
                persisted.getId(),
                persisted.getMachineName(),
                machineDefinition.xmlPath(),
                engine,
                persisted.getOpenedAt());

        runtimeBySessionId.put(persisted.getId(), runtime);
        log.info("Session opened successfully sessionId={} machine={}", persisted.getId(), persisted.getMachineName());
        return toView(runtime);
    }

    @Override
    @Transactional
    public SessionView closeSession(UUID sessionId) {
        if (sessionId == null) {
            throw new ApiValidationException("sessionId must be provided");
        }
        log.info("Closing session sessionId={}", sessionId);

        SessionEntity entity = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (SessionStatus.CLOSED.name().equals(entity.getStatus())) {
            throw new ConflictException("Session is already closed: " + sessionId);
        }

        Instant closedAt = Instant.now();
        entity.setStatus(SessionStatus.CLOSED.name());
        entity.setClosedAt(closedAt);
        SessionEntity updated = sessionRepository.save(entity);

        SessionRuntime runtime = runtimeBySessionId.remove(sessionId);
        if (runtime != null) {
            runtime.markClosed(closedAt);
        }

        log.info("Session closed sessionId={}", sessionId);
        return toView(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionView getSession(UUID sessionId) {
        if (sessionId == null) {
            throw new ApiValidationException("sessionId must be provided");
        }

        return sessionRepository.findById(sessionId)
                .map(this::toView)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionView> listSessions() {
        List<SessionView> result = new ArrayList<>();
        for (SessionEntity sessionEntity : sessionRepository.findAll()) {
            result.add(toView(sessionEntity));
        }
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
            SessionEntity persisted = sessionRepository
                    .findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

            if (SessionStatus.CLOSED.name().equals(persisted.getStatus())) {
                throw new ConflictException("Session is closed: " + sessionId);
            }

            throw new ConflictException("Session runtime is unavailable. Please open a new session.");
        }

        if (runtime.status() != SessionStatus.OPEN) {
            throw new ConflictException("Session is closed: " + sessionId);
        }

        return runtime;
    }

    private SessionView toView(SessionRuntime runtime) {
        return new SessionView(
                runtime.sessionId(),
                runtime.machineName(),
                runtime.status(),
                runtime.openedAt(),
                runtime.closedAt());
    }

    private SessionView toView(SessionEntity entity) {
        return new SessionView(
                entity.getId(),
                entity.getMachineName(),
                SessionStatus.valueOf(entity.getStatus()),
                entity.getOpenedAt(),
                entity.getClosedAt());
    }
}
