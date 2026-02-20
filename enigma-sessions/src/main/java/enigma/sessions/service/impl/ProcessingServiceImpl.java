package enigma.sessions.service.impl;

import enigma.dal.entity.ProcessRecordEntity;
import enigma.dal.repository.MachineRepository;
import enigma.dal.repository.ProcessRecordRepository;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ResourceNotFoundException;
import enigma.sessions.model.ProcessOutcome;
import enigma.sessions.model.SessionRuntime;
import enigma.sessions.service.ProcessingService;
import enigma.sessions.service.SessionService;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.MachineState;
import enigma.shared.utils.CodeStateCompactFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class ProcessingServiceImpl implements ProcessingService {

    private final SessionService sessionService;
    private final ProcessRecordRepository processRecordRepository;
    private final MachineRepository machineRepository;

    public ProcessingServiceImpl(SessionService sessionService,
                                 ProcessRecordRepository processRecordRepository,
                                 MachineRepository machineRepository) {
        this.sessionService = sessionService;
        this.processRecordRepository = processRecordRepository;
        this.machineRepository = machineRepository;
    }

    @Override
    @Transactional
    public ProcessOutcome process(UUID sessionId, String input) {
        if (input == null) {
            throw new ApiValidationException("input must be provided");
        }
        String normalizedInput = input.trim().toUpperCase(Locale.ROOT);

        SessionRuntime runtime = sessionService.resolveOpenRuntime(sessionId);

        ProcessTrace processTrace;
        long durationNanos;
        MachineState state;
        String codeUsedForProcessing;

        synchronized (runtime.lock()) {
            MachineState stateBeforeProcessing = runtime.engine().machineData();
            codeUsedForProcessing = CodeStateCompactFormatter.originalCodeCompact(stateBeforeProcessing.curCodeState());

            long startedAt = System.nanoTime();
            processTrace = runtime.engine().process(normalizedInput);
            durationNanos = System.nanoTime() - startedAt;
            state = runtime.engine().machineData();
        }

        processRecordRepository.save(new ProcessRecordEntity(
                UUID.randomUUID(),
                machineRepository.findById(runtime.machineId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Machine not found for processing runtime: " + runtime.machineId())),
                runtime.sessionId().toString(),
                codeUsedForProcessing,
                normalizedInput,
                processTrace.output(),
                durationNanos
        ));

        return new ProcessOutcome(
                runtime.sessionId(),
                runtime.machineName(),
                normalizedInput,
                processTrace.output(),
                durationNanos,
                state
        );
    }
}
