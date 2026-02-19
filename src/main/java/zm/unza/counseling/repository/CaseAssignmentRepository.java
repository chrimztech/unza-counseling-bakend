package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.CaseAssignment;
import zm.unza.counseling.entity.Counselor;
import zm.unza.counseling.entity.User;

import java.util.List;

@Repository
public interface CaseAssignmentRepository extends JpaRepository<CaseAssignment, Long> {

    List<CaseAssignment> findByCaseEntity(Case caseEntity);

    List<CaseAssignment> findByAssignedTo(Counselor assignedTo);

    List<CaseAssignment> findByAssignedBy(User assignedBy);

    List<CaseAssignment> findByCaseEntityAndStatus(Case caseEntity, String status);

    List<CaseAssignment> findByAssignedToAndStatus(Counselor assignedTo, String status);

    long countByCaseEntity(Case caseEntity);

    long countByAssignedTo(Counselor assignedTo);

    long countByAssignedBy(User assignedBy);

    long countByCaseEntityAndStatus(Case caseEntity, String status);

    void deleteByCaseEntity(Case caseEntity);
}