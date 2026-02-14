package enigma.api.controller;

import enigma.sessions.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final MaintenanceService maintenanceService;

    public DebugController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @DeleteMapping("/storage")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearStorage() {
        try {
            maintenanceService.clearStorage();
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
