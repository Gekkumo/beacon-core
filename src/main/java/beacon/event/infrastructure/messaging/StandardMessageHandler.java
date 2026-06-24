package beacon.event.infrastructure.messaging;

import beacon.common.exception.DuplicateEventException;
import beacon.common.exception.EventProcessingException;
import beacon.event.application.EventService;
import beacon.event.infrastructure.messaging.mapper.StandardMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StandardMessageHandler {

    private final EventService eventService;
    private final StandardMessageMapper mapper;

    public void handle(JsonNode root) {
        try {
            UUID clientEventId = mapper.extractEventId(root);

            if (clientEventId != null && eventService.existsByEventId(clientEventId)) {
                log.debug("Duplicate skipped: eventId={}", clientEventId);
                return;
            }

            mapper.validateRequiredFields(root);

            String aggregateId = root.get("aggregateId").asString();
            String eventType = root.get("eventType").asString();
            String source = root.get("source").asString();
            Instant occurredAt = mapper.extractOccurredAt(root);
            JsonNode payloadNode = mapper.extractPayload(root);
            var actor = mapper.extractActor(root);
            JsonNode changes = mapper.extractChanges(root);
            JsonNode context = mapper.extractContext(root);
            String ipAddress = actor != null ? actor.ipAddress() : null;
            String userAgent = actor != null ? actor.userAgent() : null;

            eventService.processEvent(
                    clientEventId,
                    aggregateId,
                    eventType,
                    payloadNode,
                    occurredAt,
                    source,
                    actor,
                    changes,
                    context,
                    null,
                    ipAddress,
                    userAgent
            );
            log.debug("Consumed standard: aggregateId={}, type={}", aggregateId, eventType);

        } catch (DuplicateEventException e) {
            log.warn("Duplicate from Kafka: eventId={}", e.getEventId());
            throw e;
        } catch (Exception e) {
            throw new EventProcessingException("Failed to process standard event", e);
        }
    }
}