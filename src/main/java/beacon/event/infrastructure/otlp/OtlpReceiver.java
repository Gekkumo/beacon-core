package beacon.event.infrastructure.otlp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/traces")
@RequiredArgsConstructor
public class OtlpReceiver {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @PostMapping(consumes = "application/json")
    public String exportJson(@RequestBody String body) {
        try {
            JsonNode root = jsonMapper.readTree(body);
            processJson(root);
            return "OK";
        } catch (Exception e) {
            log.error("Failed to parse JSON OTLP request", e);
            return "ERROR";
        }
    }

    private void processJson(JsonNode root) {
        JsonNode resourceSpans = root.get("resourceSpans");
        if (resourceSpans != null && resourceSpans.isArray()) {
            for (JsonNode rs : resourceSpans) {
                JsonNode scopeSpans = rs.get("scopeSpans");
                if (scopeSpans != null && scopeSpans.isArray()) {
                    for (JsonNode ss : scopeSpans) {
                        JsonNode spans = ss.get("spans");
                        if (spans != null && spans.isArray()) {
                            for (JsonNode spanNode : spans) {
                                processJsonSpan(spanNode);
                            }
                        }
                    }
                }
            }
        }
    }

    private void processJsonSpan(JsonNode spanNode) {
        try {
            String traceId = spanNode.has("traceId") ? spanNode.get("traceId").asString() : null;
            String spanId = spanNode.has("spanId") ? spanNode.get("spanId").asString() : null;
            String name = spanNode.has("name") ? spanNode.get("name").asString() : "unknown";

            Map<String, Object> context = new HashMap<>();
            if (traceId != null) context.put("traceId", traceId);
            if (spanId != null) context.put("spanId", spanId);
            context.put("name", name);

            JsonNode attributes = spanNode.get("attributes");
            if (attributes != null && attributes.isArray()) {
                for (JsonNode attr : attributes) {
                    String key = attr.has("key") ? attr.get("key").asString() : null;
                    JsonNode value = attr.get("value");
                    if (key != null && value != null) {
                        if (value.has("stringValue")) {
                            context.put(key, value.get("stringValue").asString());
                        } else if (value.has("intValue")) {
                            context.put(key, value.get("intValue").asLong());
                        } else if (value.has("boolValue")) {
                            context.put(key, value.get("boolValue").asBoolean());
                        } else if (value.has("doubleValue")) {
                            context.put(key, value.get("doubleValue").asDouble());
                        }
                    }
                }
            }

            if (traceId != null) {
                kafkaTemplate.send("app-transaction-context", traceId, context);
                log.debug("OTLP JSON span processed: traceId={}, name={}", traceId, name);
            }
        } catch (Exception e) {
            log.error("Failed to process JSON span", e);
        }
    }
}