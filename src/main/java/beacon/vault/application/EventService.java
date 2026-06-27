package beacon.vault.application;

import beacon.vault.domain.*;
import beacon.vault.domain.repository.EventRepository;
import beacon.vault.domain.vo.GeoLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrichmentService enrichmentService;

    @Transactional
    public EventId saveEvent(Event event) {
        if (eventRepository.existsById(event.eventId())) {
            log.warn("Duplicate event: {}", event.eventId());
            return event.eventId();
        }

        Event eventToSave = event;
        if (event.geoLocation() == null || event.geoLocation().isEmpty()) {
            GeoLocation geoLocation = enrichmentService.enrichGeoIp(event.actor().ipAddress());
            eventToSave = event.withGeoLocation(geoLocation);
        }

        eventRepository.save(eventToSave);
        log.info("Event saved: aggregateId={}, type={}, traceId={}",
                eventToSave.aggregateId().value(),
                eventToSave.eventType().value(),
                eventToSave.traceId() != null ? eventToSave.traceId().value() : "none");

        return eventToSave.eventId();
    }

    public List<Event> getAggregateHistory(AggregateId aggregateId) {
        return eventRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }

    public List<Event> getEventsByTraceId(TraceId traceId) {
        return eventRepository.findByTraceId(traceId);
    }

    public boolean existsById(EventId eventId) {
        return eventRepository.existsById(eventId);
    }
}