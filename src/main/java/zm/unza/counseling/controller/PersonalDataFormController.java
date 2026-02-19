package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.PersonalDataFormRequest;
import zm.unza.counseling.dto.response.PersonalDataFormResponse;
import zm.unza.counseling.service.PersonalDataFormService;

/**
 * Personal Data Form Controller - Handles HTTP requests for PersonalDataForm entities
 */
@RestController
@RequestMapping({"/api/v1/personal-data-forms", "/api/personal-data-forms", "/v1/personal-data-forms", "/personal-data-forms"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PersonalDataFormController {

    private final PersonalDataFormService personalDataFormService;

    /**
     * Creates a new personal data form
     * @param clientId the ID of the client
     * @param request the request DTO
     * @return the created personal data form response
     */
    @PostMapping("/clients/{clientId}")
    public ResponseEntity<PersonalDataFormResponse> createPersonalDataForm(
            @PathVariable Long clientId,
            @Valid @RequestBody PersonalDataFormRequest request) {
        PersonalDataFormResponse response = personalDataFormService.createPersonalDataFormForClient(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a personal data form by client ID
     * @param clientId the ID of the client
     * @return the personal data form response
     */
    @GetMapping("/clients/{clientId}")
    public ResponseEntity<PersonalDataFormResponse> getPersonalDataFormByClientId(@PathVariable Long clientId) {
        PersonalDataFormResponse response = personalDataFormService.getPersonalDataFormByClientId(clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a personal data form by client file number
     * @param clientFileNo the client file number
     * @return the personal data form response
     */
    @GetMapping("/file/{clientFileNo}")
    public ResponseEntity<PersonalDataFormResponse> getPersonalDataFormByClientFileNo(@PathVariable String clientFileNo) {
        PersonalDataFormResponse response = personalDataFormService.getPersonalDataFormByClientFileNo(clientFileNo);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a personal data form
     * @param clientId the ID of the client
     * @param request the request DTO
     * @return the updated personal data form response
     */
    @PutMapping("/clients/{clientId}")
    public ResponseEntity<PersonalDataFormResponse> updatePersonalDataForm(
            @PathVariable Long clientId,
            @Valid @RequestBody PersonalDataFormRequest request) {
        PersonalDataFormResponse response = personalDataFormService.updatePersonalDataForm(clientId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a personal data form
     * @param clientId the ID of the client
     * @return the API response
     */
    @DeleteMapping("/clients/{clientId}")
    public ResponseEntity<Void> deletePersonalDataForm(@PathVariable Long clientId) {
        personalDataFormService.deletePersonalDataForm(clientId);
        return ResponseEntity.noContent().build();
    }
}
