package beacon.processor.persister;

import beacon.shared.config.KafkaConfig;
import beacon.vault.application.EventService;
import beacon.vault.domain.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPersister {

    private final EventService eventService;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_ENRICHED_EVENTS,
            groupId = "beacon-persister",
            concurrency = "3"
    )
    public void persistEvent(Event event) {
        try {
            eventService.saveEvent(event);
            log.debug("Event persisted: aggregateId={}, type={}, traceId={}",
                    event.aggregateId().value(),
                    event.eventType().value(),
                    event.traceId() != null ? event.traceId().value() : "none");
        } catch (Exception e) {
            log.error("Failed to persist event: aggregateId={}, type={}",
                    event.aggregateId().value(),
                    event.eventType().value(), e);
        }
    }
}