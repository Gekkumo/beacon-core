package beacon.event.infrastructure.messaging.mapper;

import beacon.common.exception.EventProcessingException;
import beacon.event.domain.vo.Actor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class StandardMessageMapper extends EventMessageMapper {

    public StandardMessageMapper(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    public Instant extractOccurredAt(JsonNode root) {
        return Instant.parse(root.get("occurredAt").asString());
    }

    public JsonNode extractPayload(JsonNode root) {
        return root.get("payload");
    }

    public Actor extractActor(JsonNode root) {
        if (!root.has("actor") || root.get("actor").isNull()) {
            return null;
        }
        try {
            return jsonMapper.treeToValue(root.get("actor"), Actor.class);
        } catch (Exception e) {
            log.error("Failed to deserialize actor from message", e);
            return null;
        }
    }

    public JsonNode extractChanges(JsonNode root) {
        if (!root.has("changes") || root.get("changes").isNull()) {
            return jsonMapper.createObjectNode();
        }
        return root.get("changes");
    }

    public UUID extractEventId(JsonNode root) {
        if (root == null || !root.has("eventId") || root.get("eventId").isNull()) {
            return null;
        }
        try {
            return UUID.fromString(root.get("eventId").asString());
        } catch (IllegalArgumentException e) {
            log.error("Invalid eventId format: {}", root.get("eventId").asString(), e);
            return null;
        }
    }

    public void validateRequiredFields(JsonNode root) {
        if (!root.has("aggregateId") || !root.has("eventType")
                || !root.has("source") || !root.has("occurredAt")) {
            log.error("Missing required fields in event: {}", root);
            throw new EventProcessingException("Missing required fields: aggregateId, eventType, source, occurredAt");
        }
    }
}