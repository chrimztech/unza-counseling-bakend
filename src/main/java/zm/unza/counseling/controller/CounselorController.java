package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.dto.request.CreateCounselorRequest;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.dto.response.ApiResponse;
import zm.unza.counseling.service.CounselorService;

import java.util.List;

@RestController
@RequestMapping("/counselors")
@RequiredArgsConstructor
public class CounselorController {
    
    private final CounselorService counselorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Counselor>>> getAllCounselors() {
        return ResponseEntity.ok(ApiResponse.success(counselorService.getAllCounselors()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Counselor>> getCounselorById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(counselorService.getCounselorById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Counselor>> createCounselor(@RequestBody CreateCounselorRequest request) {
        Counselor counselor = counselorService.createCounselor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(counselor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCounselor(@PathVariable Long id) {
        counselorService.deleteCounselor(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
