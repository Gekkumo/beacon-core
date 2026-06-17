package beacon.event.infrastructure.messaging;

import beacon.event.domain.Outbox;
import beacon.event.domain.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaEventProducer producer;
    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    @Retryable(
            includes = {OptimisticLockingFailureException.class},
            maxRetries = 2,
            delay = 100,
            multiplier = 2.0
    )
    public void publish() {
        List<Outbox> pending = outboxRepository.findPendingWithSkipLocked("PENDING", BATCH_SIZE);

        if (pending.isEmpty()) {
            return;
        }

        for (Outbox outbox : pending) {
            try {
                producer.send(outbox.getAggregateId(), outbox.getPayload())
                        .orTimeout(5, TimeUnit.SECONDS)
                        .thenAccept(result -> {
                            markAsSent(outbox);
                            log.debug("Outbox event sent: eventId={}", outbox.getEventId());
                        })
                        .exceptionally(ex -> {
                            log.error("Failed to send outbox event: eventId={}", outbox.getEventId(), ex);
                            return null;
                        });
            } catch (Exception e) {
                log.error("Failed to publish outbox event: eventId={}", outbox.getEventId(), e);
            }
        }
    }

    @Transactional
    protected void markAsSent(Outbox outbox) {
        try {
            Outbox fresh = outboxRepository.findById(outbox.getId()).orElse(null);
            if (fresh != null && "PENDING".equals(fresh.getStatus())) {
                fresh.markSent();
                outboxRepository.save(fresh);
            }
        } catch (OptimisticLockingFailureException e) {
            log.debug("Optimistic lock failed for outbox id={}, already processed", outbox.getId());
        }
    }
}