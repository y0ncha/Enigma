package enigma.sessions.service.impl;

import enigma.dal.entity.ConfigurationEventEntity;
import enigma.dal.repository.ConfigurationEventRepository;
import enigma.sessions.model.SessionRuntime;
import enigma.sessions.service.ConfigurationService;
import enigma.sessions.service.SessionService;
import enigma.shared.dto.config.CodeConfig;
import enigma.shared.state.MachineState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final String ACTION_MANUAL = "MANUAL_CONFIG";
    private static final String ACTION_RANDOM = "RANDOM_CONFIG";
    private static final String ACTION_RESET = "RESET";

    private final SessionService sessionService;
    private final ConfigurationEventRepository configurationEventRepository;

    public ConfigurationServiceImpl(SessionService sessionService,
                                    ConfigurationEventRepository configurationEventRepository) {
        this.sessionService = sessionService;
        this.configurationEventRepository = configurationEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MachineState currentState(UUID sessionId) {
        SessionRuntime runtime = sessionService.resolveOpenRuntime(sessionId);
        synchronized (runtime.lock()) {
            return runtime.engine().machineData();
        }
    }

    @Override
    @Transactional
    public MachineState configureManual(UUID sessionId, CodeConfig config) {
        SessionRuntime runtime = sessionService.resolveOpenRuntime(sessionId);

        MachineState state;
        synchronized (runtime.lock()) {
            runtime.engine().configManual(config);
            state = runtime.engine().machineData();
        }

        configurationEventRepository.save(new ConfigurationEventEntity(
                runtime.sessionId(),
                runtime.machineName(),
                ACTION_MANUAL,
                config.toString(),
                Instant.now()));

        return state;
    }

    @Override
    @Transactional
    public MachineState configureRandom(UUID sessionId) {
        SessionRuntime runtime = sessionService.resolveOpenRuntime(sessionId);

        MachineState state;
        synchronized (runtime.lock()) {
            runtime.engine().configRandom();
            state = runtime.engine().machineData();
        }

        String payload = state.ogCodeState() == null ? "<not configured>" : state.ogCodeState().toString();
        configurationEventRepository.save(new ConfigurationEventEntity(
                runtime.sessionId(),
                runtime.machineName(),
                ACTION_RANDOM,
                payload,
                Instant.now()));

        return state;
    }

    @Override
    @Transactional
    public MachineState reset(UUID sessionId) {
        SessionRuntime runtime = sessionService.resolveOpenRuntime(sessionId);

        MachineState state;
        synchronized (runtime.lock()) {
            runtime.engine().reset();
            state = runtime.engine().machineData();
        }

        configurationEventRepository.save(new ConfigurationEventEntity(
                runtime.sessionId(),
                runtime.machineName(),
                ACTION_RESET,
                "RESET_TO_ORIGINAL",
                Instant.now()));

        return state;
    }
}
