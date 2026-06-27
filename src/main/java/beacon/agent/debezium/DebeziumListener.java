package beacon.agent.debezium;

import beacon.shared.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Slf4j
@Component
@RequiredArgsConstructor
public class DebeziumListener {

    private final KafkaTemplate<String, JsonNode> kafkaTemplate;

    @KafkaListener(
            topics = {"todoapp.todoapp.tasks", "todoapp.todoapp.users"},
            groupId = "beacon-debezium",
            concurrency = "1"
    )
    public void listenDebezium(JsonNode root) {
        try {
            if (root != null && (root.has("op") || root.has("before") || root.has("after"))) {
                String key = extractAggregateId(root);

                kafkaTemplate.send(
                        KafkaConfig.TOPIC_DEBEZIUM_EVENTS,
                        key,
                        root
                );
                log.debug("Debezium event forwarded to: {}, key: {}", KafkaConfig.TOPIC_DEBEZIUM_EVENTS, key);
            } else {
                log.warn("Invalid Debezium message, skipping");
            }
        } catch (Exception e) {
            log.error("Failed to process Debezium message", e);
        }
    }

    private String extractAggregateId(JsonNode root) {
        if (root.has("after") && !root.get("after").isNull()) {
            JsonNode after = root.get("after");
            String id = tryExtractId(after);
            if (id != null) {
                return id;
            }
        }

        if (root.has("before") && !root.get("before").isNull()) {
            JsonNode before = root.get("before");
            String id = tryExtractId(before);
            if (id != null) {
                return id;
            }
        }

        return java.util.UUID.randomUUID().toString();
    }

    private String tryExtractId(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String[] idFields = {"id", "uuid", "task_id", "user_id", "project_id"};
        for (String field : idFields) {
            if (node.has(field) && !node.get(field).isNull()) {
                JsonNode value = node.get(field);
                if (value.isString()) {
                    return value.asString();
                }
                if (value.isNumber()) {
                    return value.asString();
                }
            }
        }

        return null;
    }
}