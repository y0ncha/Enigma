package enigma.sessions.service.impl;

import enigma.dal.entity.ProcessRecordEntity;
import enigma.dal.repository.ProcessRecordRepository;
import enigma.sessions.model.ConfigEventView;
import enigma.sessions.model.HistoryView;
import enigma.sessions.model.MachineDefinition;
import enigma.sessions.model.ProcessRecordView;
import enigma.sessions.model.SessionView;
import enigma.sessions.service.HistoryQueryService;
import enigma.sessions.service.MachineCatalogService;
import enigma.sessions.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HistoryQueryServiceImpl implements HistoryQueryService {

    private final MachineCatalogService machineCatalogService;
    private final SessionService sessionService;
    private final ConfigurationEventStore configurationEventStore;
    private final ProcessRecordRepository processRecordRepository;

    public HistoryQueryServiceImpl(MachineCatalogService machineCatalogService,
                                   SessionService sessionService,
                                   ConfigurationEventStore configurationEventStore,
                                   ProcessRecordRepository processRecordRepository) {
        this.machineCatalogService = machineCatalogService;
        this.sessionService = sessionService;
        this.configurationEventStore = configurationEventStore;
        this.processRecordRepository = processRecordRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryView bySession(UUID sessionId) {
        SessionView session = sessionService.getSession(sessionId);

        List<ConfigEventView> configEvents = configurationEventStore.bySession(sessionId);

        List<ProcessRecordView> processEvents = processRecordRepository
                .findBySessionIdOrderByIdAsc(sessionId.toString())
                .stream()
                .map(entity -> toProcessView(entity, session.machineName()))
                .toList();

        return new HistoryView(
                "SESSION",
                sessionId,
                session.machineName(),
                configEvents,
                processEvents
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryView byMachineName(String machineName) {
        MachineDefinition machineDefinition = machineCatalogService.resolveMachine(machineName);

        List<ConfigEventView> configEvents = configurationEventStore.byMachineName(machineDefinition.machineName());

        List<ProcessRecordView> processEvents = processRecordRepository
                .findByMachine_IdOrderByIdAsc(machineDefinition.machineId())
                .stream()
                .map(entity -> toProcessView(entity, machineDefinition.machineName()))
                .toList();

        return new HistoryView(
                "MACHINE",
                null,
                machineDefinition.machineName(),
                configEvents,
                processEvents
        );
    }

    private ProcessRecordView toProcessView(ProcessRecordEntity entity, String machineName) {
        return new ProcessRecordView(
                entity.getId(),
                parseUuidOrNull(entity.getSessionId()),
                machineName,
                entity.getCode(),
                entity.getInputText(),
                entity.getOutputText(),
                entity.getDurationNanos(),
                null
        );
    }

    private UUID parseUuidOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
