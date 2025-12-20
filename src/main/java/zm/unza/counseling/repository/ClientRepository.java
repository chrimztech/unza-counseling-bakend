package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}