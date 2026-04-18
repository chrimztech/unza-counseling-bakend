package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.counseling.dto.request.ClientIntakeFormRequest;
import zm.unza.counseling.dto.response.ClientIntakeFormResponse;
import zm.unza.counseling.service.ClientIntakeFormService;

/**
 * CRUD endpoints for the counselor-completed client intake form.
 */
@RestController
@RequestMapping({
        "/api/v1/client-intake-forms",
        "/api/client-intake-forms",
        "/v1/client-intake-forms",
        "/client-intake-forms"
})
@CrossOrigin(origins = "*")
public class ClientIntakeFormController {

    private final ClientIntakeFormService clientIntakeFormService;

    public ClientIntakeFormController(ClientIntakeFormService clientIntakeFormService) {
        this.clientIntakeFormService = clientIntakeFormService;
    }

    @PostMapping("/clients/{clientId}")
    public ResponseEntity<ClientIntakeFormResponse> createForClient(
            @PathVariable Long clientId,
            @Valid @RequestBody ClientIntakeFormRequest request
    ) {
        ClientIntakeFormResponse response = clientIntakeFormService.createForClient(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientIntakeFormResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientIntakeFormService.getById(id));
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientIntakeFormResponse> getByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientIntakeFormService.getByClientId(clientId));
    }

    @GetMapping("/cases/{caseId}")
    public ResponseEntity<ClientIntakeFormResponse> getByCaseId(@PathVariable Long caseId) {
        return ResponseEntity.ok(clientIntakeFormService.getByCaseId(caseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientIntakeFormResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientIntakeFormRequest request
    ) {
        return ResponseEntity.ok(clientIntakeFormService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientIntakeFormService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
