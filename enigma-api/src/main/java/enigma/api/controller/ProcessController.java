package enigma.api.controller;

import enigma.api.dto.request.ProcessRequest;
import enigma.api.dto.response.ProcessResponse;
import enigma.api.dto.response.ResponseMapper;
import enigma.sessions.service.ProcessingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class ProcessController {

    private final ProcessingService processingService;

    public ProcessController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping
    public ProcessResponse process(@Valid @RequestBody ProcessRequest request) {
        return ResponseMapper.process(processingService.process(request.sessionId(), request.input()));
    }
}
