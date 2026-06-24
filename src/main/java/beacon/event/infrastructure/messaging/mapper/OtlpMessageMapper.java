package beacon.event.infrastructure.messaging.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Slf4j
@Component
public class OtlpMessageMapper {

    private final JsonMapper jsonMapper;

    public OtlpMessageMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String extractTraceId(JsonNode root) {
        return root.has("traceId") ? root.get("traceId").asString() : null;
    }

    public String extractAggregateId(JsonNode root) {
        return root.has("aggregateId") ? root.get("aggregateId").asString() : null;
    }

    public String extractEventType(JsonNode root) {
        return root.has("eventType") ? root.get("eventType").asString() : null;
    }

    public JsonNode extractPayload(JsonNode root) {
        return root.has("payload") ? root.get("payload") : jsonMapper.createObjectNode();
    }

    public Instant extractOccurredAt(JsonNode root) {
        if (root.has("occurredAt")) {
            try {
                return Instant.parse(root.get("occurredAt").asString());
            } catch (Exception e) {
                log.warn("Failed to parse occurredAt: {}", root.get("occurredAt"));
            }
        }
        return Instant.now();
    }

    public String extractSource(JsonNode root) {
        return root.has("source") ? root.get("source").asString() : "otlp";
    }

    public String extractActorId(JsonNode root) {
        return root.has("actorId") ? root.get("actorId").asString() : null;
    }

    public String extractActorType(JsonNode root) {
        return root.has("actorType") ? root.get("actorType").asString() : null;
    }

    public String extractActorName(JsonNode root) {
        return root.has("actorName") ? root.get("actorName").asString() : null;
    }

    public String extractIpAddress(JsonNode root) {
        return root.has("ipAddress") ? root.get("ipAddress").asString() : null;
    }

    public String extractUserAgent(JsonNode root) {
        return root.has("userAgent") ? root.get("userAgent").asString() : null;
    }

    public JsonNode extractChanges(JsonNode root) {
        return root.has("changes") ? root.get("changes") : jsonMapper.createObjectNode();
    }

    public JsonNode extractContext(JsonNode root) {
        return root.has("context") ? root.get("context") : jsonMapper.createObjectNode();
    }
}