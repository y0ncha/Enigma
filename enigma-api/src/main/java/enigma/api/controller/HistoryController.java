package enigma.api.controller;

import enigma.api.dto.response.HistoryResponse;
import enigma.api.dto.response.ResponseMapper;
import enigma.sessions.service.HistoryQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryQueryService historyQueryService;

    public HistoryController(HistoryQueryService historyQueryService) {
        this.historyQueryService = historyQueryService;
    }

    @GetMapping("/session/{sessionId}")
    public HistoryResponse bySession(@PathVariable UUID sessionId) {
        return ResponseMapper.history(historyQueryService.bySession(sessionId));
    }

    @GetMapping("/machine/{machineName}")
    public HistoryResponse byMachine(@PathVariable String machineName) {
        return ResponseMapper.history(historyQueryService.byMachineName(machineName));
    }
}
