package enigma.sessions.service.impl;

import enigma.dal.entity.ConfigurationEventEntity;
import enigma.dal.entity.ProcessRecordEntity;
import enigma.dal.entity.SessionEntity;
import enigma.dal.repository.ConfigurationEventRepository;
import enigma.dal.repository.ProcessRecordRepository;
import enigma.dal.repository.SessionRepository;
import enigma.sessions.exception.ResourceNotFoundException;
import enigma.sessions.model.ConfigEventView;
import enigma.sessions.model.HistoryView;
import enigma.sessions.model.ProcessRecordView;
import enigma.sessions.service.HistoryQueryService;
import enigma.sessions.service.MachineCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HistoryQueryServiceImpl implements HistoryQueryService {

    private final MachineCatalogService machineCatalogService;
    private final SessionRepository sessionRepository;
    private final ConfigurationEventRepository configurationEventRepository;
    private final ProcessRecordRepository processRecordRepository;

    public HistoryQueryServiceImpl(MachineCatalogService machineCatalogService,
                                   SessionRepository sessionRepository,
                                   ConfigurationEventRepository configurationEventRepository,
                                   ProcessRecordRepository processRecordRepository) {
        this.machineCatalogService = machineCatalogService;
        this.sessionRepository = sessionRepository;
        this.configurationEventRepository = configurationEventRepository;
        this.processRecordRepository = processRecordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryView bySession(UUID sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        List<ConfigEventView> configEvents = configurationEventRepository
                .findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toConfigView)
                .toList();

        List<ProcessRecordView> processEvents = processRecordRepository
                .findBySessionIdOrderByProcessedAtAsc(sessionId)
                .stream()
                .map(this::toProcessView)
                .toList();

        return new HistoryView(
                "SESSION",
                sessionId,
                session.getMachineName(),
                configEvents,
                processEvents
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryView byMachineName(String machineName) {
        machineCatalogService.resolveMachine(machineName);

        List<ConfigEventView> configEvents = configurationEventRepository
                .findByMachineNameOrderByCreatedAtAsc(machineName)
                .stream()
                .map(this::toConfigView)
                .toList();

        List<ProcessRecordView> processEvents = processRecordRepository
                .findByMachineNameOrderByProcessedAtAsc(machineName)
                .stream()
                .map(this::toProcessView)
                .toList();

        return new HistoryView(
                "MACHINE",
                null,
                machineName,
                configEvents,
                processEvents
        );
    }

    private ConfigEventView toConfigView(ConfigurationEventEntity entity) {
        return new ConfigEventView(
                entity.getId(),
                entity.getSessionId(),
                entity.getMachineName(),
                entity.getAction(),
                entity.getPayload(),
                entity.getCreatedAt()
        );
    }

    private ProcessRecordView toProcessView(ProcessRecordEntity entity) {
        return new ProcessRecordView(
                entity.getId(),
                entity.getSessionId(),
                entity.getMachineName(),
                entity.getInputText(),
                entity.getOutputText(),
                entity.getDurationNanos(),
                entity.getProcessedAt()
        );
    }
}
