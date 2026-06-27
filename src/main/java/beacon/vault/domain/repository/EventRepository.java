package beacon.vault.domain.repository;

import beacon.vault.domain.AggregateId;
import beacon.vault.domain.Event;
import beacon.vault.domain.EventId;
import beacon.vault.domain.TraceId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    void save(Event event);

    Optional<Event> findById(EventId eventId);

    List<Event> findByAggregateId(AggregateId aggregateId);

    List<Event> findByAggregateIdOrderByOccurredAtAsc(AggregateId aggregateId);

    List<Event> findByTraceId(TraceId traceId);

    List<Event> findByTimeRange(Instant from, Instant to);

    boolean existsById(EventId eventId);
}