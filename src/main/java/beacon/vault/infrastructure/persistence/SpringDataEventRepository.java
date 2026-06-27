package beacon.vault.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataEventRepository extends JpaRepository<JpaEventEntity, UUID> {

    List<JpaEventEntity> findByAggregateId(String aggregateId);

    List<JpaEventEntity> findByAggregateIdOrderByOccurredAtAsc(String aggregateId);

    List<JpaEventEntity> findByTraceId(String traceId);

    List<JpaEventEntity> findByOccurredAtBetween(Instant from, Instant to);

    boolean existsByEventId(UUID eventId);
}