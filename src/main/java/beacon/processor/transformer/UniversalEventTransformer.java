package beacon.processor.transformer;

import beacon.processor.model.TransformedEvent;
import beacon.vault.domain.EventType;
import beacon.vault.domain.vo.Actor;
import beacon.vault.domain.vo.Source;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class UniversalEventTransformer {

    private static final Pattern TRACE_ID_PATTERN =
            Pattern.compile("traceparent=['\"]([a-f0-9]{32})");

    private static final Pattern TRACE_PARENT_PATTERN =
            Pattern.compile("traceparent=([a-f0-9]{32})");

    /**
     * Трансформирует сырое сообщение Debezium в TransformedEvent.
     */
    public TransformedEvent transformDebezium(JsonNode root) {
        if (root == null) {
            log.warn("Invalid Debezium message: null");
            return null;
        }

        if (!root.has("op") && !root.has("before") && !root.has("after")) {
            log.warn("Invalid Debezium message: no op/after/before");
            return null;
        }

        String operation = extractOperation(root);
        if (operation == null) {
            log.debug("Unknown operation, skipping");
            return null;
        }

        return TransformedEvent.builder()
                .aggregateId(extractAggregateId(root))
                .eventType(EventType.fromDebeziumOperation(operation))
                .payload(extractPayload(root))
                .occurredAt(extractOccurredAt(root))
                .source(Source.of(extractSource(root)))
                .actor(Actor.system())
                .traceId(extractTraceId(root))
                .changes(null)
                .context(null)
                .build();
    }

    private String extractOperation(JsonNode root) {
        if (root.has("op") && !root.get("op").isNull()) {
            return root.get("op").asString();
        }
        if (root.has("__op") && !root.get("__op").isNull()) {
            return root.get("__op").asString();
        }
        return null;
    }

    private String extractAggregateId(JsonNode root) {
        JsonNode data = extractPayload(root);
        if (data == null || data.isNull()) {
            return "unknown-" + Instant.now().toEpochMilli();
        }

        String[] idFields = {"id", "uuid", "task_id", "user_id", "project_id"};
        for (String field : idFields) {
            if (data.has(field) && !data.get(field).isNull()) {
                return data.get(field).asString();
            }
        }
        return "event-" + Instant.now().toEpochMilli();
    }

    private JsonNode extractPayload(JsonNode root) {
        if (root.has("after") && !root.get("after").isNull()) {
            return root.get("after");
        }
        if (root.has("before") && !root.get("before").isNull()) {
            return root.get("before");
        }
        return root;
    }

    private Instant extractOccurredAt(JsonNode root) {
        JsonNode data = extractPayload(root);
        if (data == null || data.isNull()) {
            return Instant.now();
        }

        String[] timeFields = {"created_at", "updated_at", "timestamp", "event_time", "ts"};
        for (String field : timeFields) {
            if (data.has(field) && !data.get(field).isNull()) {
                JsonNode value = data.get(field);
                if (value.isNumber()) {
                    long ts = value.asLong();
                    if (ts > 1_000_000_000_000L) return Instant.ofEpochMilli(ts);
                    if (ts > 1_000_000_000L) return Instant.ofEpochSecond(ts);
                    return Instant.ofEpochSecond(ts / 1_000_000, (ts % 1_000_000) * 1000);
                }
                if (value.isString()) {
                    try {
                        return Instant.parse(value.asString());
                    } catch (Exception ignored) {}
                }
            }
        }
        return Instant.now();
    }

    private String extractSource(JsonNode root) {
        if (root.has("source") && !root.get("source").isNull()) {
            JsonNode source = root.get("source");
            String db = source.has("db") ? source.get("db").asString() : "debezium";
            String table = source.has("table") ? "." + source.get("table").asString() : "";
            return db + table;
        }
        return "debezium";
    }

    /**
     * Извлекает trace_id из tracingspancontext
     * Формат: "traceparent=00-{traceId}-{spanId}-{flags}"
     */
    private String extractTraceId(JsonNode root) {
        JsonNode data = extractPayload(root);

        // 1. Парсим tracingspancontext
        if (data != null && !data.isNull() && data.has("tracingspancontext") && !data.get("tracingspancontext").isNull()) {
            String tracingContext = data.get("tracingspancontext").asString();
            // Формат: "traceparent=00-{traceId}-{spanId}-{flags}\n"
            // Нам нужен traceId (32 символа)
            Pattern pattern = Pattern.compile("traceparent=00-([a-f0-9]{32})");
            Matcher matcher = pattern.matcher(tracingContext);
            if (matcher.find()) {
                String traceId = matcher.group(1);
                log.debug("Extracted trace_id from tracingspancontext: {}", traceId);
                return traceId;
            }

            // Альтернативный поиск без префикса 00-
            Pattern altPattern = Pattern.compile("traceparent=([a-f0-9]{32})");
            Matcher altMatcher = altPattern.matcher(tracingContext);
            if (altMatcher.find()) {
                String traceId = altMatcher.group(1);
                log.debug("Extracted trace_id from tracingspancontext (alt): {}", traceId);
                return traceId;
            }

            // Если не нашли — логируем значение
            log.warn("Could not extract trace_id from tracingspancontext: {}", tracingContext);
        }

        // 2. Прямые поля trace_id или traceId
        if (data != null && !data.isNull()) {
            if (data.has("trace_id") && !data.get("trace_id").isNull()) {
                return data.get("trace_id").asString();
            }
            if (data.has("traceId") && !data.get("traceId").isNull()) {
                return data.get("traceId").asString();
            }
        }

        // 3. SQL комментарии
        if (root.has("sql") && !root.get("sql").isNull()) {
            String sql = root.get("sql").asString();
            Matcher matcher = TRACE_ID_PATTERN.matcher(sql);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }
}