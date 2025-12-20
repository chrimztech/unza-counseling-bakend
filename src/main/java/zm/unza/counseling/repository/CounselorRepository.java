package zm.unza.counseling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zm.unza.counseling.entity.Counselor;

@Repository
public interface CounselorRepository extends JpaRepository<Counselor, Long> {
}