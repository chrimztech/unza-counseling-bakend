package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.CaseDocument;

import java.util.List;

@Repository
public interface CaseDocumentRepository extends JpaRepository<CaseDocument, Long> {

    List<CaseDocument> findByCaseEntity(Case caseEntity);

    List<CaseDocument> findByCaseEntityAndIsPublic(Case caseEntity, Boolean isPublic);

    List<CaseDocument> findByUploadedBy(Long uploadedBy);

    List<CaseDocument> findByCaseEntityAndFileNameContaining(Case caseEntity, String fileName);

    long countByCaseEntity(Case caseEntity);

    long countByUploadedBy(Long uploadedBy);

    void deleteByCaseEntity(Case caseEntity);
}