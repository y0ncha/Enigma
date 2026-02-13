package enigma.api.controller;

import enigma.api.dto.request.ManualConfigRequest;
import enigma.api.dto.request.SessionCommandRequest;
import enigma.api.dto.response.MachineStateResponse;
import enigma.api.dto.response.ResponseMapper;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.service.ConfigurationService;
import enigma.shared.dto.config.CodeConfig;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/config")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostMapping("/manual")
    public MachineStateResponse manual(@Valid @RequestBody ManualConfigRequest request) {
        return ResponseMapper.machineState(configurationService.configureManual(
                request.sessionId(),
                toCodeConfig(request)
        ));
    }

    @PostMapping("/random")
    public MachineStateResponse random(@Valid @RequestBody SessionCommandRequest request) {
        return ResponseMapper.machineState(configurationService.configureRandom(request.sessionId()));
    }

    @PostMapping("/reset")
    public MachineStateResponse reset(@Valid @RequestBody SessionCommandRequest request) {
        return ResponseMapper.machineState(configurationService.reset(request.sessionId()));
    }

    private CodeConfig toCodeConfig(ManualConfigRequest request) {
        String positionsRaw = request.positions();
        if (positionsRaw == null || positionsRaw.isBlank()) {
            throw new ApiValidationException("positions must be provided");
        }

        List<Character> positions = new ArrayList<>();
        for (char c : positionsRaw.toCharArray()) {
            positions.add(c);
        }

        List<Integer> rotorIds = List.copyOf(request.rotorIds());
        String reflectorId = request.reflectorId().trim();
        String plugboard = request.plugboard() == null ? "" : request.plugboard().trim();

        return new CodeConfig(rotorIds, positions, reflectorId, plugboard);
    }
}
