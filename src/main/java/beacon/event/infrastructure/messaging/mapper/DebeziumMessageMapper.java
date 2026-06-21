package beacon.event.infrastructure.messaging.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@Slf4j
@Component
public class DebeziumMessageMapper extends EventMessageMapper {

    public DebeziumMessageMapper(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    public boolean isDebeziumMessage(JsonNode root) {
        return root != null && root.has("payload") && root.has("schema");
    }

    public JsonNode extractDebeziumPayload(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        if (root.has("payload") && !root.get("payload").isNull()) {
            return root.get("payload");
        }
        return root;
    }

    public String extractAggregateIdFromDebezium(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return null;
        }
        if (payload.has("id") && !payload.get("id").isNull()) {
            return payload.get("id").asString();
        }
        if (payload.has("uuid") && !payload.get("uuid").isNull()) {
            return payload.get("uuid").asString();
        }
        return "event-" + Instant.now().toEpochMilli();
    }

    public String extractEventTypeFromDebezium(JsonNode root) {
        if (root == null || root.isNull()) {
            return "DEBEZIUM_EVENT";
        }

        JsonNode payload = extractDebeziumPayload(root);
        if (payload == null || payload.isNull()) {
            return "DEBEZIUM_EVENT";
        }

        JsonNode opNode = payload.get("__op");
        if (opNode != null && !opNode.isNull()) {
            String op = opNode.asString();
            return switch (op) {
                case "c" -> "CREATE";
                case "u" -> "UPDATE";
                case "d" -> "DELETE";
                case "r" -> "SNAPSHOT";
                default -> "DEBEZIUM_EVENT";
            };
        }

        JsonNode opNodeFallback = payload.get("op");
        if (opNodeFallback != null && !opNodeFallback.isNull()) {
            String op = opNodeFallback.asString();
            return switch (op) {
                case "c" -> "CREATE";
                case "u" -> "UPDATE";
                case "d" -> "DELETE";
                case "r" -> "SNAPSHOT";
                default -> "DEBEZIUM_EVENT";
            };
        }

        return "DEBEZIUM_EVENT";
    }

    public String extractSourceFromDebezium(JsonNode root) {
        if (root == null || root.isNull()) {
            return "debezium";
        }

        JsonNode payload = extractDebeziumPayload(root);
        if (payload != null && !payload.isNull()) {
            JsonNode dbNode = payload.get("__source_db");
            JsonNode tableNode = payload.get("__source_table");
            if (dbNode != null && !dbNode.isNull()) {
                if (tableNode != null && !tableNode.isNull()) {
                    return dbNode.asString() + "." + tableNode.asString();
                }
                return dbNode.asString();
            }
        }

        if (root.has("source") && !root.get("source").isNull()) {
            JsonNode source = root.get("source");
            if (source.has("db")) {
                return source.get("db").asString();
            }
            return source.asString();
        }

        return "debezium";
    }

    public Instant extractOccurredAtFromDebezium(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return Instant.now();
        }

        String[] possibleFields = {"created_at", "updated_at", "timestamp", "ts", "event_time"};
        for (String field : possibleFields) {
            if (payload.has(field) && !payload.get(field).isNull()) {
                JsonNode value = payload.get(field);
                if (value.isNumber()) {
                    long millis = value.asLong();
                    if (millis > 1_000_000_000_000L) {
                        return Instant.ofEpochMilli(millis);
                    }
                    if (millis > 1_000_000_000L) {
                        return Instant.ofEpochSecond(millis);
                    }
                    return Instant.ofEpochSecond(millis / 1_000_000, (millis % 1_000_000) * 1000);
                }
                if (value.isString()) {
                    try {
                        return Instant.parse(value.asString());
                    } catch (Exception e) {
                        log.warn("Failed to parse timestamp: {}", value.asString());
                    }
                }
            }
        }
        return Instant.now();
    }

    public JsonNode extractPayloadForStorage(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return jsonMapper.createObjectNode();
        }

        if (payload.has("before") && !payload.get("before").isNull()) {
            return payload.get("before");
        }

        if (payload.has("after") && !payload.get("after").isNull()) {
            return payload.get("after");
        }

        return payload;
    }
}