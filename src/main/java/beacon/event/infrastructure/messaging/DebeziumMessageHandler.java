package beacon.event.infrastructure.messaging;

import beacon.common.exception.DuplicateEventException;
import beacon.common.exception.EventProcessingException;
import beacon.event.application.EventService;
import beacon.event.infrastructure.messaging.mapper.DebeziumMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumMessageHandler {

    private final EventService eventService;
    private final DebeziumMessageMapper mapper;
    private final JsonMapper jsonMapper;

    public void handle(JsonNode root) {
        try {
            if (root == null || root.isNull()) {
                log.debug("Tombstone event, skipping");
                return;
            }

            JsonNode payload = mapper.extractDebeziumPayload(root);
            if (payload == null || payload.isNull()) {
                log.debug("No payload in Debezium message, skipping");
                return;
            }

            UUID clientEventId = UUID.randomUUID();
            String aggregateId = mapper.extractAggregateIdFromDebezium(payload);
            String eventType = mapper.extractEventTypeFromDebezium(root);
            String source = mapper.extractSourceFromDebezium(root);
            Instant occurredAt = mapper.extractOccurredAtFromDebezium(payload);
            JsonNode payloadNode = mapper.extractPayloadForStorage(payload);
            JsonNode changes = jsonMapper.createObjectNode();
            JsonNode context = jsonMapper.createObjectNode();

            eventService.processEvent(
                    clientEventId,
                    aggregateId,
                    eventType,
                    payloadNode,
                    occurredAt,
                    source,
                    null,
                    changes,
                    context
            );
            log.debug("Consumed Debezium: aggregateId={}, type={}", aggregateId, eventType);

        } catch (DuplicateEventException e) {
            log.warn("Duplicate from Kafka: eventId={}", e.getEventId());
        } catch (EventProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new EventProcessingException("Failed to process Debezium event", e);
        }
    }
}