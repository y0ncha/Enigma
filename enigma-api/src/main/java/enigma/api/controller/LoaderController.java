package enigma.api.controller;

import enigma.api.dto.request.LoadMachineRequest;
import enigma.api.dto.response.MachineResponse;
import enigma.api.dto.response.ResponseMapper;
import enigma.sessions.service.MachineCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/load")
public class LoaderController {

    private final MachineCatalogService machineCatalogService;

    public LoaderController(MachineCatalogService machineCatalogService) {
        this.machineCatalogService = machineCatalogService;
    }

    @PostMapping
    public MachineResponse loadMachine(@Valid @RequestBody LoadMachineRequest request) {
        return ResponseMapper.machine(machineCatalogService.loadMachine(request.xmlPath()));
    }

    @GetMapping
    public List<MachineResponse> listMachines() {
        return machineCatalogService.listMachines().stream()
                .map(ResponseMapper::machine)
                .toList();
    }
}
