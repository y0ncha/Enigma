package enigma.api.controller;

import enigma.api.dto.response.ApiContractMapper;
import enigma.api.dto.response.ProcessApiResponse;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.model.SessionStatus;
import enigma.sessions.service.ProcessingService;
import enigma.sessions.service.SessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.UUID;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private final ProcessingService processingService;
    private final SessionService sessionService;

    public ProcessController(ProcessingService processingService,
                             SessionService sessionService) {
        this.processingService = processingService;
        this.sessionService = sessionService;
    }

    @PostMapping
    public ProcessApiResponse process(@RequestParam("input") String input) {
        return ApiContractMapper.process(processingService.process(latestOpenSessionId(), input));
    }

    private UUID latestOpenSessionId() {
        return sessionService.listSessions().stream()
                .filter(session -> session.status() == SessionStatus.OPEN)
                .max(Comparator.comparing(session -> session.openedAt()))
                .map(session -> session.sessionId())
                .orElseThrow(() -> new ApiValidationException("No open session available"));
    }
}
