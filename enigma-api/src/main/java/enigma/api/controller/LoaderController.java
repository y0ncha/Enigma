package enigma.api.controller;

import enigma.api.dto.response.LoadMachineApiResponse;
import enigma.api.dto.response.MachineResponse;
import enigma.api.dto.response.ResponseMapper;
import enigma.sessions.service.MachineCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/load")
public class LoaderController {

    private final MachineCatalogService machineCatalogService;

    public LoaderController(MachineCatalogService machineCatalogService) {
        this.machineCatalogService = machineCatalogService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoadMachineApiResponse> loadMachine(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoadMachineApiResponse(false, null, "File not provided"));
        }

        try {
            Path uploaded = persistUpload(file);
            String machineName = machineCatalogService.loadMachine(uploaded.toString()).machineName();
            return ResponseEntity.ok(new LoadMachineApiResponse(true, machineName, null));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoadMachineApiResponse(false, null, e.getMessage()));
        }
    }

    @GetMapping
    public List<MachineResponse> listMachines() {
        return machineCatalogService.listMachines().stream()
                .map(ResponseMapper::machine)
                .toList();
    }

    private Path persistUpload(MultipartFile file) throws IOException {
        Path uploadsDir = Path.of(System.getProperty("java.io.tmpdir"), "enigma-machine-uploads");
        Files.createDirectories(uploadsDir);

        String filename = file.getOriginalFilename() == null ? "machine.xml" : file.getOriginalFilename();
        String sanitizedName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path uploaded = uploadsDir.resolve(UUID.randomUUID() + "-" + sanitizedName);

        Files.copy(file.getInputStream(), uploaded, StandardCopyOption.REPLACE_EXISTING);
        return uploaded;
    }
}
