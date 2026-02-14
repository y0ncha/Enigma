package enigma.api.controller;

import enigma.api.dto.response.ApiContractMapper;
import enigma.api.dto.response.HistoryEntryResponse;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.service.HistoryQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryQueryService historyQueryService;

    public HistoryController(HistoryQueryService historyQueryService) {
        this.historyQueryService = historyQueryService;
    }

    @GetMapping
    public Map<String, List<HistoryEntryResponse>> history(
            @RequestParam(name = "sessionID", required = false) String sessionID,
            @RequestParam(name = "machineName", required = false) String machineName) {
        boolean hasSession = sessionID != null && !sessionID.isBlank();
        boolean hasMachine = machineName != null && !machineName.isBlank();

        if (hasSession == hasMachine) {
            throw new ApiValidationException("Exactly one of sessionID or machineName must be provided");
        }

        if (hasSession) {
            return ApiContractMapper.history(historyQueryService.bySession(parseSessionId(sessionID)));
        }
        return ApiContractMapper.history(historyQueryService.byMachineName(machineName.trim()));
    }

    private UUID parseSessionId(String sessionID) {
        try {
            return UUID.fromString(sessionID.trim());
        }
        catch (IllegalArgumentException e) {
            throw new ApiValidationException("Invalid sessionID: " + sessionID);
        }
    }
}
