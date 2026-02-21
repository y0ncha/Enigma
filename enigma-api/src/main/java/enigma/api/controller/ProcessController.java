package enigma.api.controller;

import enigma.api.dto.response.ApiContractMapper;
import enigma.api.dto.response.ProcessApiResponse;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.service.ProcessingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private final ProcessingService processingService;

    public ProcessController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping
    public ProcessApiResponse process(@RequestParam("input") String input,
                                      @RequestParam("sessionID") String sessionID) {
        return ApiContractMapper.process(processingService.process(parseSessionId(sessionID), input));
    }

    private UUID parseSessionId(String sessionID) {
        if (sessionID == null || sessionID.isBlank()) {
            throw new ApiValidationException("sessionID must be provided");
        }
        try {
            return UUID.fromString(sessionID.trim());
        }
        catch (IllegalArgumentException e) {
            throw new ApiValidationException("Invalid sessionID: " + sessionID);
        }
    }
}
