package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Case;
import zm.unza.counseling.entity.PersonalDataForm;
import zm.unza.counseling.entity.Client;

import java.util.Optional;

/**
 * Personal Data Form Repository - Provides CRUD operations for PersonalDataForm entities
 */
@Repository
public interface PersonalDataFormRepository extends JpaRepository<PersonalDataForm, Long> {

    /**
     * Finds a personal data form by client
     * @param client the client associated with the form
     * @return Optional containing the personal data form if found
     */
    Optional<PersonalDataForm> findByClient(Client client);

    /**
     * Finds a personal data form by client file number
     * @param clientFileNo the client file number
     * @return Optional containing the personal data form if found
     */
    Optional<PersonalDataForm> findByClientFileNo(String clientFileNo);

    /**
     * Finds a personal data form by case
     * @param caseEntity the case associated with the form
     * @return Optional containing the personal data form if found
     */
    Optional<PersonalDataForm> findByCaseEntity(Case caseEntity);

    /**
     * Finds a personal data form by client ID
     * @param clientId the client ID
     * @return Optional containing the personal data form if found
     */
    Optional<PersonalDataForm> findByClientId(Long clientId);
}
