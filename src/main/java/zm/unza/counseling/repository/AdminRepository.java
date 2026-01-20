package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
}
