package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.Counselor;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    Optional<Case> findByCaseNumber(String caseNumber);

    List<Case> findByClient(Client client);

    long countByClient(Client client);

    boolean existsByClient(Client client);

    List<Case> findByCounselor(Counselor counselor);

    boolean existsByCounselor(Counselor counselor);

    List<Case> findByStatus(Case.CaseStatus status);

    List<Case> findByPriority(Case.CasePriority priority);

    List<Case> findByCounselorAndStatus(Counselor counselor, Case.CaseStatus status);

    long countByCounselor(Counselor counselor);

    long countByStatus(Case.CaseStatus status);

    long countByAssignedBy(Long assignedBy);

    @Query("SELECT c FROM Case c WHERE c.client = :client AND c.status IN :statuses " +
            "ORDER BY COALESCE(c.lastActivityAt, c.updatedAt, c.createdAt) DESC")
    List<Case> findRecentCasesByClientAndStatuses(
            @Param("client") Client client,
            @Param("statuses") List<Case.CaseStatus> statuses
    );
}
