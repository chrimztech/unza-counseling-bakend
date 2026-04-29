package zm.unza.counseling.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.entity.Role;
import zm.unza.counseling.entity.User;
import zm.unza.counseling.exception.ResourceNotFoundException;
import zm.unza.counseling.exception.ValidationException;
import zm.unza.counseling.repository.CounselorRepository;
import zm.unza.counseling.repository.UserRepository;

import java.util.List;

/**
 * Bridges counselor-role user records into the Counselor subtype.
 */
@Service
public class CounselorIdentityService {

    private final CounselorRepository counselorRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public CounselorIdentityService(CounselorRepository counselorRepository, UserRepository userRepository) {
        this.counselorRepository = counselorRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<Counselor> getAllCounselors() {
        userRepository.findByRolesName(Role.ERole.ROLE_COUNSELOR)
                .forEach(user -> getOrCreateCounselor(user.getId()));
        return counselorRepository.findAll();
    }

    @Transactional
    public Counselor getOrCreateCounselor(Long userId) {
        return counselorRepository.findById(userId)
                .orElseGet(() -> promoteUserToCounselor(userId));
    }

    private Counselor promoteUserToCounselor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Counselor not found with id: " + userId));

        if (!user.isCounselor()) {
            throw new ValidationException("Selected user is not configured as a counselor");
        }

        entityManager.createNativeQuery("""
                UPDATE users
                SET user_type = 'COUNSELOR',
                    available_for_appointments = COALESCE(available_for_appointments, true)
                WHERE id = :userId
                """)
                .setParameter("userId", userId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        return counselorRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Counselor not found with id: " + userId));
    }
}
