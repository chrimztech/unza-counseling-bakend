package zm.unza.counseling.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.CreateAdminRequest;
import zm.unza.counseling.dto.response.MessageAuditDto;
import zm.unza.counseling.entity.Admin;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.AdminService;
import zm.unza.counseling.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/admin","/api/admin","/v1/admin","/admin"})
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final MessageService messageService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageImpl<Admin>>> getAllAdmins() {
        return ResponseEntity.ok(ApiResponse.success(new PageImpl<>(adminService.getAllAdmins())));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Admin>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        Admin admin = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(admin));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/messages/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MessageAuditDto>>> getMessageAuditRecords(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessagesForAudit(query, senderId, recipientId, start, end)
        ));
    }

    @GetMapping("/messages/audit/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getMessageAuditStats() {
        return ResponseEntity.ok(ApiResponse.success(messageService.getMessageAuditStats()));
    }
}
