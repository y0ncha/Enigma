package enigma.api.controller;

import enigma.api.dto.request.OpenSessionRequest;
import enigma.api.dto.response.ResponseMapper;
import enigma.api.dto.response.SessionResponse;
import enigma.sessions.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/open")
    public SessionResponse open(@Valid @RequestBody OpenSessionRequest request) {
        return ResponseMapper.session(sessionService.openSession(request.machineName()));
    }

    @DeleteMapping("/{sessionId}")
    public SessionResponse close(@PathVariable UUID sessionId) {
        return ResponseMapper.session(sessionService.closeSession(sessionId));
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
}
