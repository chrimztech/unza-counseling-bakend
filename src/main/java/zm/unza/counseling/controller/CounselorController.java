package zm.unza.counseling.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.service.CounselorService;

import java.util.List;

@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorController {
    
    private final CounselorService counselorService;

    @GetMapping
    public ResponseEntity<List<Counselor>> getAllCounselors() {
        return ResponseEntity.ok(counselorService.getAllCounselors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Counselor> getCounselorById(@PathVariable Long id) {
        return ResponseEntity.ok(counselorService.getCounselorById(id));
    }
}
