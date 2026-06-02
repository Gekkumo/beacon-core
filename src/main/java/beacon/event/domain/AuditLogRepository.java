package beacon.event.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByActorIdOrderByOccurredAtDesc(String actorId);

    List<AuditLog> findByResourceTypeAndResourceIdOrderByOccurredAtDesc(String resourceType, String resourceId);

    List<AuditLog> findByActionAndOccurredAtBetweenOrderByOccurredAtDesc(String action, Instant from, Instant to);

    boolean existsByEventId(UUID eventId);
}
