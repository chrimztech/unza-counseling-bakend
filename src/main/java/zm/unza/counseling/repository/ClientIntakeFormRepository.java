package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.Client;
import zm.unza.counseling.entity.ClientIntakeForm;

import java.util.Optional;

@Repository
public interface ClientIntakeFormRepository extends JpaRepository<ClientIntakeForm, Long> {

    Optional<ClientIntakeForm> findByClient(Client client);

    Optional<ClientIntakeForm> findByClientId(Long clientId);

    Optional<ClientIntakeForm> findByCaseEntity(Case caseEntity);

    boolean existsByClientId(Long clientId);

    boolean existsByCounselor(zm.unza.counseling.entity.Counselor counselor);
}
