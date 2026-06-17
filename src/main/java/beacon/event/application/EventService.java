package beacon.event.application;

import beacon.common.exception.DuplicateEventException;
import beacon.common.exception.EventProcessingException;
import beacon.event.domain.*;
import beacon.event.domain.vo.Actor;
import beacon.event.domain.vo.Change;
import beacon.event.infrastructure.messaging.EventMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
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
    private final AuditLogRepository auditLogRepository;
    private final EventMessageMapper mapper;
    private final OutboxRepository outboxRepository;
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
            List<Change> changes,
            JsonNode context
    ) {
        if (clientEventId != null && eventRepository.existsByEventId(clientEventId)) {
            log.warn("Duplicate event detected: eventId={}", clientEventId);
            throw new DuplicateEventException(clientEventId);
        }

        UUID finalEventId = clientEventId != null ? clientEventId : UUID.randomUUID();

        Event event = Event.builder()
                .eventId(finalEventId)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payloadNode)
                .occurredAt(occurredAt)
                .source(source)
                .build();
        eventRepository.save(event);

        Actor finalActor = actor != null ? actor : Actor.system();
        AuditLog auditLog = AuditLog.builder()
                .eventId(event.getEventId())
                .actorId(finalActor.actorId())
                .actorType(finalActor.actorType())
                .actorName(finalActor.actorName())
                .action(finalActor.action())
                .resourceType(finalActor.resourceType())
                .resourceId(finalActor.resourceId())
                .service(source)
                .ipAddress(finalActor.ipAddress())
                .userAgent(finalActor.userAgent())
                .changes(mapper.serializeChanges(changes))
                .context(context != null ? context : jsonMapper.createObjectNode())
                .occurredAt(occurredAt)
                .build();
        auditLogRepository.save(auditLog);

        try {
            JsonNode eventJson = jsonMapper.valueToTree(Map.of(
                    "eventId", event.getEventId().toString(),
                    "aggregateId", aggregateId,
                    "eventType", eventType,
                    "payload", payloadNode,
                    "occurredAt", occurredAt.toString(),
                    "source", source,
                    "actor", finalActor,
                    "changes", changes != null ? changes : List.of(),
                    "context", context != null ? context : jsonMapper.createObjectNode()
            ));
            Outbox outbox = Outbox.builder()
                    .eventId(event.getEventId())
                    .aggregateId(aggregateId)
                    .payload(eventJson)
                    .build();
            outboxRepository.save(outbox);
        } catch (JacksonException e) {
            log.error("Failed to serialize outbox payload for event {}", event.getEventId(), e);
            throw new EventProcessingException("Failed to create outbox record", e);
        }

        return event.getEventId();
    }

    public boolean existsByEventId(UUID eventId) {
        return eventRepository.existsByEventId(eventId);
    }

    public List<Event> getAggregateHistory(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
    }
}