package beacon.event.infrastructure.messaging;

import beacon.common.exception.DuplicateEventException;
import beacon.common.exception.EventProcessingException;
import beacon.event.infrastructure.messaging.mapper.DebeziumMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPersistenceConsumer {

    private final DebeziumMessageMapper debeziumMapper;
    private final DebeziumMessageHandler debeziumHandler;
    private final StandardMessageHandler standardHandler;

    @KafkaListener(
            topicPattern = "source-a.*",
            groupId = "beacon-core")
    public void consume(JsonNode root) {
        try {
            if (root == null || root.isNull()) {
                log.debug("Tombstone event, skipping");
                return;
            }

            boolean isDebezium = debeziumMapper.isDebeziumMessage(root);

            if (isDebezium) {
                debeziumHandler.handle(root);
            } else {
                standardHandler.handle(root);
            }

        } catch (DuplicateEventException e) {
            log.warn("Duplicate from Kafka: eventId={}", e.getEventId());
        } catch (EventProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new EventProcessingException("Failed to process event", e);
        }
    }
}