package beacon.agent.otlp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraceConverter {

    private final JsonMapper jsonMapper;

    public ObjectNode convertToContext(JsonNode root) {
        ObjectNode context = jsonMapper.createObjectNode();

        JsonNode resourceSpans = root.get("resourceSpans");
        if (resourceSpans == null || !resourceSpans.isArray()) {
            log.warn("No resourceSpans found in OTLP payload");
            return context;
        }

        for (JsonNode rs : resourceSpans) {
            JsonNode scopeSpans = rs.get("scopeSpans");
            if (scopeSpans == null || !scopeSpans.isArray()) {
                continue;
            }

            for (JsonNode ss : scopeSpans) {
                JsonNode spans = ss.get("spans");
                if (spans == null || !spans.isArray()) {
                    continue;
                }

                for (JsonNode span : spans) {
                    extractSpanContext(span, context);
                }
            }
        }

        return context;
    }

    private void extractSpanContext(JsonNode span, ObjectNode context) {
        if (span.has("traceId") && !span.get("traceId").isNull()) {
            context.put("traceId", span.get("traceId").asString());
        }

        if (span.has("spanId") && !span.get("spanId").isNull()) {
            context.put("spanId", span.get("spanId").asString());
        }

        if (span.has("name") && !span.get("name").isNull()) {
            context.put("name", span.get("name").asString());
        }

        JsonNode attributes = span.get("attributes");
        if (attributes != null && attributes.isArray()) {
            for (JsonNode attr : attributes) {
                extractAttribute(attr, context);
            }
        }
    }

    private void extractAttribute(JsonNode attr, ObjectNode context) {
        if (!attr.has("key") || attr.get("key").isNull()) {
            return;
        }

        String key = attr.get("key").asString();
        JsonNode value = attr.get("value");

        if (value == null || value.isNull()) {
            return;
        }

        if (value.has("stringValue")) {
            context.put(key, value.get("stringValue").asString());
        } else if (value.has("intValue")) {
            context.put(key, value.get("intValue").asLong());
        } else if (value.has("doubleValue")) {
            context.put(key, value.get("doubleValue").asDouble());
        } else if (value.has("boolValue")) {
            context.put(key, value.get("boolValue").asBoolean());
        }
    }

    public boolean isValidOtlp(JsonNode root) {
        return root != null &&
                root.has("resourceSpans") &&
                root.get("resourceSpans").isArray() &&
                !root.get("resourceSpans").isEmpty();
    }
}