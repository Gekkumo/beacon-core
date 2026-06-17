package beacon.event.infrastructure.messaging;

import beacon.common.exception.DuplicateEventException;
import beacon.common.exception.EventProcessingException;
import beacon.event.application.EventService;
import beacon.event.domain.vo.Actor;
import beacon.event.domain.vo.Change;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPersistenceConsumer {

    private final EventService eventService;
    private final EventMessageMapper mapper;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_RAW_EVENTS,
            groupId = "beacon-core")
    public void consume(JsonNode root) {
        try {
            if (root == null || root.isNull()) {
                log.debug("Received tombstone event (null value), skipping processing");
                return;
            }

            String eventIdStr = extractEventId(root);
            UUID clientEventId = eventIdStr != null ? UUID.fromString(eventIdStr) : null;

            if (clientEventId != null && eventService.existsByEventId(clientEventId)) {
                log.debug("Duplicate event skipped: eventId={}", eventIdStr);
                return;
            }

            validateRequiredFields(root);

            String aggregateId = root.get("aggregateId").asString();
            String eventType = root.get("eventType").asString();
            String source = root.get("source").asString();
            Instant occurredAt = mapper.extractOccurredAt(root);
            JsonNode payloadNode = mapper.extractPayload(root);
            Actor actor = mapper.extractActor(root);
            List<Change> changes = mapper.extractChanges(root);
            JsonNode context = mapper.extractContext(root);

            eventService.processEvent(
                    clientEventId,
                    aggregateId,
                    eventType,
                    payloadNode,
                    occurredAt,
                    source,
                    actor,
                    changes,
                    context
            );
            log.debug("Consumed and persisted event: aggregateId={}, type={}", aggregateId, eventType);

        } catch (DuplicateEventException e) {
            log.warn("Duplicate event from Kafka: eventId={}", e.getEventId());
        } catch (EventProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new EventProcessingException("Failed to process event", e);
        }
    }

    private String extractEventId(JsonNode root) {
        if (root == null) {
            return null;
        }

        JsonNode eventIdNode = root.get("eventId");
        if (eventIdNode == null || eventIdNode.isNull()) {
            return null;
        }

        return eventIdNode.asString();
    }

    private void validateRequiredFields(JsonNode root) {
        if (!root.has("aggregateId") || !root.has("eventType")
                || !root.has("source") || !root.has("occurredAt")) {
            log.error("Missing required fields in event: {}", root);
            throw new EventProcessingException("Missing required fields: aggregateId, eventType, source, occurredAt");
        }
    }
}