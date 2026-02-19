package zm.unza.counseling.controller;

import zm.unza.counseling.dto.request.CaseDocumentRequest;
import zm.unza.counseling.dto.response.CaseDocumentResponse;
import zm.unza.counseling.service.CaseDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/cases/documents", "/api/cases/documents", "/v1/cases/documents", "/cases/documents"})
public class CaseDocumentController {

    private final CaseDocumentService caseDocumentService;

    public CaseDocumentController(CaseDocumentService caseDocumentService) {
        this.caseDocumentService = caseDocumentService;
    }

    @PostMapping
    public ResponseEntity<CaseDocumentResponse> uploadDocument(
            @RequestParam Long caseId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "false") Boolean isPublic,
            @RequestParam Long uploadedBy) {
        
        CaseDocumentResponse response = caseDocumentService.uploadDocument(
                caseId, file, uploadedBy, description, isPublic);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaseDocumentResponse> getDocumentById(@PathVariable Long id) {
        CaseDocumentResponse response = caseDocumentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<CaseDocumentResponse>> getDocumentsByCase(@PathVariable Long caseId) {
        List<CaseDocumentResponse> documents = caseDocumentService.getDocumentsByCase(caseId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/case/{caseId}/public")
    public ResponseEntity<List<CaseDocumentResponse>> getPublicDocumentsByCase(@PathVariable Long caseId) {
        List<CaseDocumentResponse> documents = caseDocumentService.getPublicDocumentsByCase(caseId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/uploaded-by/{uploadedBy}")
    public ResponseEntity<List<CaseDocumentResponse>> getDocumentsByUploadedBy(@PathVariable Long uploadedBy) {
        List<CaseDocumentResponse> documents = caseDocumentService.getDocumentsByUploadedBy(uploadedBy);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CaseDocumentResponse>> searchDocumentsByCaseAndFileName(
            @RequestParam Long caseId,
            @RequestParam String fileName) {
        List<CaseDocumentResponse> documents = caseDocumentService.searchDocumentsByCaseAndFileName(caseId, fileName);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        caseDocumentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/case/{caseId}")
    public ResponseEntity<Void> deleteDocumentsByCase(@PathVariable Long caseId) {
        caseDocumentService.deleteDocumentsByCase(caseId);
        return ResponseEntity.noContent().build();
    }
}