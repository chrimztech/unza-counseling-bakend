package zm.unza.counseling.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Transactional(readOnly = true)
    public List<Counselor> getAllCounselors() {
        return counselorRepository.findAll();
    }

    @Transactional
    public List<Counselor> syncAndGetAllCounselors() {
        userRepository.findByRolesName(Role.ERole.ROLE_COUNSELOR)
                .forEach(user -> getOrCreateCounselor(user.getId()));
        return counselorRepository.findAll();
    }

    // REQUIRES_NEW: this method is called from several read-only query paths
    // (e.g. CaseService#getCasesByCounselor) to lazily provision a Counselor
    // row for a user on first access. Joining the caller's read-only
    // transaction would make the UPDATE below fail with "cannot execute
    // UPDATE in a read-only transaction", so it always runs in its own,
    // independently-committed writable transaction instead.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
