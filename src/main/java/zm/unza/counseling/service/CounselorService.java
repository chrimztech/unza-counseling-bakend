package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.CounselorRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorService {
    
    private final CounselorRepository counselorRepository;

    public List<Counselor> getAllCounselors() {
        return counselorRepository.findAll();
    }

    public Counselor getCounselorById(Long id) {
        return counselorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Counselor not found with id: " + id));
    }
}
