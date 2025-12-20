package zm.unza.counseling.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Client;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByStudentId(String studentId);
    
    @Query("SELECT c FROM Client c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.studentId) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Client> searchClients(String query, Pageable pageable);
    
    Page<Client> findByClientStatus(Client.ClientStatus status, Pageable pageable);
    
    Page<Client> findByRiskLevel(Client.RiskLevel riskLevel, Pageable pageable);
    
    long countByClientStatus(Client.ClientStatus status);
    
    @Query("SELECT COUNT(c) FROM Client c WHERE c.riskLevel IN :riskLevels")
    long countByRiskLevels(List<Client.RiskLevel> riskLevels);
}