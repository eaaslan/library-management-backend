package tr.com.eaaslan.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.com.eaaslan.library.service.AccountMaintenanceService;
import tr.com.eaaslan.library.service.PenaltyService;

@RestController
@RequestMapping("/admin/testing")
@PreAuthorize("hasRole('ADMIN')")
public class TestController {

    private final PenaltyService penaltyService;
    private final AccountMaintenanceService accountMaintenanceService;

    public TestController(PenaltyService penaltyService, AccountMaintenanceService accountMaintenanceService) {
        this.penaltyService = penaltyService;
        this.accountMaintenanceService = accountMaintenanceService;
    }

    @PostMapping("/run-penalty-check")
    public ResponseEntity<String> runPenaltyCheck() {
        penaltyService.applyLatePenalties();
        return ResponseEntity.ok("Penalty check completed");
    }

    @PostMapping("/run-suspension-check")
    public ResponseEntity<String> runSuspensionCheck() {
        penaltyService.checkSuspensionExpirations();
        return ResponseEntity.ok("Suspension check completed");
    }

    @PostMapping("/run-account-maintenance")
    public ResponseEntity<String> runAccountMaintenance() {
        accountMaintenanceService.handleInactiveAccounts();
        return ResponseEntity.ok("Account maintenance completed");
    }
}