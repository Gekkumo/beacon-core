package beacon.vault.infrastructure.persistence;

import beacon.vault.domain.AggregateId;
import beacon.vault.domain.Event;
import beacon.vault.domain.EventId;
import beacon.vault.domain.TraceId;
import beacon.vault.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaEventRepository implements EventRepository {

    private final SpringDataEventRepository springRepository;
    private final EventMapper eventMapper;

    @Override
    public void save(Event event) {
        JpaEventEntity entity = eventMapper.toEntity(event);
        springRepository.save(entity);
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        return springRepository.findById(eventId.value())
                .map(eventMapper::toDomain);
    }

    @Override
    public List<Event> findByAggregateId(AggregateId aggregateId) {
        return springRepository.findByAggregateId(aggregateId.value())
                .stream()
                .map(eventMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByAggregateIdOrderByOccurredAtAsc(AggregateId aggregateId) {
        return springRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId.value())
                .stream()
                .map(eventMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByTraceId(TraceId traceId) {
        return springRepository.findByTraceId(traceId.value())
                .stream()
                .map(eventMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByTimeRange(Instant from, Instant to) {
        return springRepository.findByOccurredAtBetween(from, to)
                .stream()
                .map(eventMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(EventId eventId) {
        return springRepository.existsById(eventId.value());
    }
}