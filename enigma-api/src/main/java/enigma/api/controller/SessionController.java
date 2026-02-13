package enigma.api.controller;

import enigma.api.dto.request.CreateSessionApiRequest;
import enigma.api.dto.response.CreateSessionApiResponse;
import enigma.api.dto.response.ResponseMapper;
import enigma.api.dto.response.SessionResponse;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.exception.ConflictException;
import enigma.sessions.exception.ResourceNotFoundException;
import enigma.sessions.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public CreateSessionApiResponse create(@Valid @RequestBody CreateSessionApiRequest request) {
        try {
            return new CreateSessionApiResponse(sessionService.openSession(request.machine()).sessionId().toString());
        }
        catch (ResourceNotFoundException e) {
            throw new ConflictException("Unknown machine name: " + request.machine());
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("sessionID") String sessionID) {
        UUID parsedId = parseSessionId(sessionID);
        try {
            sessionService.closeSession(parsedId);
        }
        catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Unknown sessionID: " + sessionID);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sessionId}")
    public SessionResponse get(@PathVariable UUID sessionId) {
        return ResponseMapper.session(sessionService.getSession(sessionId));
    }

    @GetMapping
    public List<SessionResponse> list() {
        return sessionService.listSessions().stream()
                .map(ResponseMapper::session)
                .toList();
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
