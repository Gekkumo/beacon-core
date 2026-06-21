package beacon.event.infrastructure.messaging.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class EventMessageMapper {

    protected final JsonMapper jsonMapper;

    public EventMessageMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public JsonNode extractContext(JsonNode root) {
        if (!root.has("context") || root.get("context").isNull()) {
            return jsonMapper.createObjectNode();
        }
        return root.get("context");
    }
}