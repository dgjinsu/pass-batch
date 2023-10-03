package pt.passbatch.pass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BulkPassRepository extends JpaRepository<BulkPassEntity, Integer> {
//    @Query("SELECT bp FROM BulkPassEntity bp WHERE bp.status = :status AND bp.startedAt > :startedAt")

    List<BulkPassEntity> findByStatusAndStartedAtGreaterThan(BulkPassStatus status, LocalDateTime startedAt);
}
