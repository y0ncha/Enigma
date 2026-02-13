package enigma.sessions.service.impl;

import enigma.dal.entity.ProcessRecordEntity;
import enigma.dal.repository.ProcessRecordRepository;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.model.ProcessOutcome;
import enigma.sessions.model.SessionRuntime;
import enigma.sessions.service.ProcessingService;
import enigma.sessions.service.SessionService;
import enigma.shared.dto.tracer.ProcessTrace;
import enigma.shared.state.MachineState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessingServiceImpl implements ProcessingService {

    private final SessionService sessionService;
    private final ProcessRecordRepository processRecordRepository;

    public ProcessingServiceImpl(SessionService sessionService,
                                 ProcessRecordRepository processRecordRepository) {
        this.sessionService = sessionService;
        this.processRecordRepository = processRecordRepository;
    }

    @Override
    @Transactional
    public ProcessOutcome process(UUID sessionId, String input) {
        if (input == null) {
            throw new ApiValidationException("input must be provided");
        }

        SessionRuntime runtime = sessionService.resolveOpenRuntime(sessionId);

        ProcessTrace processTrace;
        long durationNanos;
        MachineState state;

        synchronized (runtime.lock()) {
            long startedAt = System.nanoTime();
            processTrace = runtime.engine().process(input);
            durationNanos = System.nanoTime() - startedAt;
            state = runtime.engine().machineData();
        }

        processRecordRepository.save(new ProcessRecordEntity(
                runtime.sessionId(),
                runtime.machineName(),
                input,
                processTrace.output(),
                durationNanos,
                Instant.now()
        ));

        return new ProcessOutcome(
                runtime.sessionId(),
                runtime.machineName(),
                input,
                processTrace.output(),
                durationNanos,
                state
        );
    }
}
