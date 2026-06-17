package beacon.event.infrastructure.messaging;

import beacon.event.domain.vo.Actor;
import beacon.event.domain.vo.Change;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventMessageMapper {

    private final JsonMapper jsonMapper;

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
        } catch (JacksonException e) {
            log.error("Failed to deserialize actor from message", e);
            return null;
        }
    }

    public List<Change> extractChanges(JsonNode root) {
        if (!root.has("changes") || root.get("changes").isNull()) {
            return Collections.emptyList();
        }

        JsonNode changesNode = root.get("changes");
        if (changesNode.isArray()) {
            try {
                return jsonMapper.readValue(
                        changesNode.toString(),
                        jsonMapper.getTypeFactory().constructCollectionType(List.class, Change.class)
                );
            } catch (JacksonException e) {
                log.error("Failed to deserialize changes from message", e);
                return Collections.emptyList();
            }
        }

        return Collections.emptyList();
    }

    public JsonNode extractContext(JsonNode root) {
        if (!root.has("context") || root.get("context").isNull()) {
            return jsonMapper.createObjectNode();
        }
        return root.get("context");
    }

    public JsonNode serializeChanges(List<Change> changes) {
        if (changes == null || changes.isEmpty()) {
            return jsonMapper.createObjectNode();
        }
        try {
            return jsonMapper.valueToTree(changes);
        } catch (JacksonException e) {
            log.error("Failed to serialize changes to JSON", e);
            return jsonMapper.createObjectNode();
        }
    }
}