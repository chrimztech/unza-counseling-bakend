package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.AiCounsellingScenario;

import java.util.List;

@Repository
public interface AiCounsellingScenarioRepository extends JpaRepository<AiCounsellingScenario, Long> {

    /** Retrieval pool: enabled scenarios that have a counsellor response. */
    List<AiCounsellingScenario> findByEnabledTrueAndHasResponseTrue();

    long countByHasResponseTrue();

    long countByEnabledTrueAndHasResponseTrue();

    long countByAiDraftedTrue();

    @Modifying
    @Query("delete from AiCounsellingScenario s where s.source = :source")
    int deleteBySource(@Param("source") String source);

    @Query("select s from AiCounsellingScenario s where "
            + "(:category is null or s.problemCategory = :category) and "
            + "(:risk is null or s.riskLevel = :risk) and "
            + "(:hasResponse is null or s.hasResponse = :hasResponse) and "
            + "(:aiDrafted is null or coalesce(s.aiDrafted, false) = :aiDrafted) and "
            + "(:q is null or lower(s.studentMessage) like :q or lower(s.idealResponse) like :q or lower(s.keyTopics) like :q)")
    Page<AiCounsellingScenario> search(@Param("category") String category,
                                       @Param("risk") String risk,
                                       @Param("hasResponse") Boolean hasResponse,
                                       @Param("aiDrafted") Boolean aiDrafted,
                                       @Param("q") String q,
                                       Pageable pageable);

    @Query("select s.problemCategory, count(s) from AiCounsellingScenario s group by s.problemCategory order by count(s) desc")
    List<Object[]> countByCategory();

    @Query("select s.riskLevel, count(s) from AiCounsellingScenario s group by s.riskLevel")
    List<Object[]> countByRisk();
}
