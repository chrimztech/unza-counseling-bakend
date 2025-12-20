package zm.unza.counseling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.dto.response.ClientResponse;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.service.ClientService;

import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ClientController {
    
    private final ClientService clientService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get all clients with pagination and filtering")
    public ResponseEntity<Page<Client>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Client.ClientStatus status,
            @RequestParam(required = false) Client.RiskLevel riskLevel
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Client> clients;
        if (search != null && !search.isEmpty()) {
            clients = clientService.searchClients(search, pageable);
        } else if (status != null) {
            clients = clientService.getClientsByStatus(status, pageable);
        } else if (riskLevel != null) {
            clients = clientService.getClientsByRiskLevel(riskLevel, pageable);
        } else {
            clients = clientService.getAllClients(pageable);
        }
        
        return ResponseEntity.ok(clients);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR') or #id == authentication.principal.id")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<Client> getClientById(@PathVariable String id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get client by student ID")
    public ResponseEntity<Client> getClientByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(clientService.getClientByStudentId(studentId));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update client information")
    public ResponseEntity<Client> updateClient(
            @PathVariable String id,
            @RequestBody Client updates
    ) {
        return ResponseEntity.ok(clientService.updateClient(id, updates));
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @Operation(summary = "Get client statistics")
    public ResponseEntity<ApiResponse> getClientStats() {
        Long activeCount = clientService.getActiveClientCount();
        Long highRiskCount = clientService.getHighRiskClientCount();
        
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "activeClients", activeCount,
                        "highRiskClients", highRiskCount
                )
        ));
    }
}