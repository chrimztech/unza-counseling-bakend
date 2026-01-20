package zm.unza.counseling.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.dto.request.AssessmentSubmissionRequest;
import zm.unza.counseling.entity.AssessmentResponse;
import zm.unza.counseling.entity.SelfAssessment;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.repository.AssessmentResponseRepository;
import zm.unza.counseling.repository.SelfAssessmentRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SelfAssessmentService {
    
    private final SelfAssessmentRepository selfAssessmentRepository;
    private final AssessmentResponseRepository assessmentResponseRepository;

    public List<SelfAssessment> getAllAssessments() {
        return selfAssessmentRepository.findAll();
    }

    public SelfAssessment createAssessment(SelfAssessment assessment) {
        return selfAssessmentRepository.save(assessment);
    }

    public SelfAssessment getAssessmentById(Long id) {
        return selfAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with id: " + id));
    }

    @Transactional
    public void submitAssessment(AssessmentSubmissionRequest request) {
        SelfAssessment assessment = getAssessmentById(request.getAssessmentId());
        
        if (request.getAnswers() != null) {
            for (Map.Entry<String, Object> entry : request.getAnswers().entrySet()) {
                AssessmentResponse response = new AssessmentResponse();
                response.setAssessment(assessment);
                response.setQuestion(entry.getKey());
                response.setAnswer(String.valueOf(entry.getValue()));
                assessmentResponseRepository.save(response);
            }
        }
    }
}
