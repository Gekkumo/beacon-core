package beacon.event.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPersistenceConsumer {

    @KafkaListener(topicPattern = "todoapp.todoapp.*", groupId = "beacon-core")
    public void consume(JsonNode root) {
        log.debug("Debezium message received: {}", root);
    }

    @KafkaListener(topics = "app-transaction-context", groupId = "beacon-core")
    public void consumeOtlp(String message) {
        log.debug("OTLP message received: {}", message);
    }
}