package beacon.event.application;

import beacon.common.exception.DuplicateEventException;
import beacon.event.domain.Event;
import beacon.event.domain.EventRepository;
import beacon.event.domain.vo.Actor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrichmentService enrichmentService;
    private final JsonMapper jsonMapper;

    @Transactional
    public UUID processEvent(
            UUID clientEventId,
            String aggregateId,
            String eventType,
            JsonNode payloadNode,
            Instant occurredAt,
            String source,
            Actor actor,
            JsonNode changes,
            JsonNode context,
            String traceId,
            String ipAddress,
            String userAgent
    ) {
        if (clientEventId != null && eventRepository.existsByEventId(clientEventId)) {
            log.warn("Duplicate event detected: eventId={}", clientEventId);
            throw new DuplicateEventException(clientEventId);
        }

        UUID finalEventId = clientEventId != null ? clientEventId : UUID.randomUUID();
        Actor finalActor = actor != null ? actor : Actor.system();

        String finalIp = ipAddress != null ? ipAddress : finalActor.ipAddress();
        String finalUserAgent = userAgent != null ? userAgent : finalActor.userAgent();

        Map<String, String> geo = enrichmentService.enrichGeoip(finalIp);
        String parsedUserAgent = enrichmentService.parseUserAgent(finalUserAgent);

        Event event = Event.builder()
                .eventId(finalEventId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadNode)
                .occurredAt(occurredAt)
                .source(source)
                .actorId(finalActor.actorId())
                .actorType(finalActor.actorType())
                .actorName(finalActor.actorName())
                .ipAddress(finalIp)
                .userAgent(parsedUserAgent != null ? parsedUserAgent : finalUserAgent)
                .status("SUCCESS")
                .changes(changes)
                .context(context != null ? context : jsonMapper.createObjectNode())
                .traceId(traceId)
                .geoipCountry(geo.get("country"))
                .geoipCity(geo.get("city"))
                .build();

        eventRepository.save(event);

        return event.getEventId();
    }

    public boolean existsByEventId(UUID eventId) {
        return eventRepository.existsByEventId(eventId);
    }

    public List<Event> getAggregateHistory(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }
}