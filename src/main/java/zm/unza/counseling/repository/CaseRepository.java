package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Case> findByCounselor(Counselor counselor);

    List<Case> findByStatus(Case.CaseStatus status);

    List<Case> findByCounselorAndStatus(Counselor counselor, Case.CaseStatus status);

    long countByCounselor(Counselor counselor);

    long countByStatus(Case.CaseStatus status);
}
