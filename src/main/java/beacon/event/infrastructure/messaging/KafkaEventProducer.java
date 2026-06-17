package beacon.event.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CompletableFuture<Void> send(String aggregateId, Object event) {
        return kafkaTemplate.send(KafkaTopicConfig.TOPIC_RAW_EVENTS, aggregateId, event)
                .thenAccept(result -> log.debug("Event sent to Kafka: aggregateId={}, offset={}",
                        aggregateId, result.getRecordMetadata().offset()))
                .exceptionally(ex -> {
                    log.error("Failed to send event to Kafka: aggregateId={}", aggregateId, ex);
                    return null;
                });
    }
}