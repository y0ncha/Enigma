package enigma.api.controller;

import enigma.api.dto.request.ManualConfigApiRequest;
import enigma.api.dto.request.PlugConnectionRequest;
import enigma.api.dto.request.RotorSelectionRequest;
import enigma.api.dto.response.ApiContractMapper;
import enigma.api.dto.response.ConfigStatusApiResponse;
import enigma.sessions.exception.ApiValidationException;
import enigma.sessions.service.ConfigurationService;
import enigma.shared.dto.config.CodeConfig;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/config")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    public ConfigStatusApiResponse status(@RequestParam("sessionID") String sessionID,
                                          @RequestParam(name = "verbose", defaultValue = "false") boolean verbose) {
        return ApiContractMapper.configStatus(configurationService.currentState(parseSessionId(sessionID)), verbose);
    }

    @PutMapping(value = "/manual", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String manual(@Valid @RequestBody ManualConfigApiRequest request) {
        configurationService.configureManual(parseSessionId(request.sessionID()), toCodeConfig(request));
        return "Manual code set successfully";
    }

    @PutMapping(value = "/automatic", produces = MediaType.TEXT_PLAIN_VALUE)
    public String automatic(@RequestParam("sessionID") String sessionID) {
        configurationService.configureRandom(parseSessionId(sessionID));
        return "Automatic code setup completed successfully";
    }

    @PutMapping(value = "/reset", produces = MediaType.TEXT_PLAIN_VALUE)
    public String reset(@RequestParam("sessionID") String sessionID) {
        configurationService.reset(parseSessionId(sessionID));
        return "Automatic code setup completed successfully";
    }

    private CodeConfig toCodeConfig(ManualConfigApiRequest request) {
        List<RotorSelectionRequest> rotors = request.rotors();
        if (rotors == null || rotors.isEmpty()) {
            throw new ApiValidationException("rotors must contain values");
        }

        List<Integer> rotorIds = new ArrayList<>(rotors.size());
        List<Character> positions = new ArrayList<>();
        for (RotorSelectionRequest rotor : rotors) {
            String rotorPosition = rotor.rotorPosition().trim();
            if (rotorPosition.length() != 1) {
                throw new ApiValidationException("rotorPosition must be a single character");
            }
            rotorIds.add(rotor.rotorNumber());
            positions.add(rotorPosition.charAt(0));
        }

        String reflectorId = request.reflector().trim();
        String plugboard = toPlugboard(request.plugs());

        return new CodeConfig(rotorIds, positions, reflectorId, plugboard);
    }

    private String toPlugboard(List<PlugConnectionRequest> plugs) {
        if (plugs == null || plugs.isEmpty()) {
            return "";
        }
        return plugs.stream()
                .map(plug -> plug.plug1().trim() + plug.plug2().trim())
                .collect(Collectors.joining());
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
