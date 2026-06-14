package beacon.event.api.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record EventSummary(
        UUID eventId,
        String eventType,
        Object payload,
        Instant occurredAt,
        Instant receivedAt,
        String source
) {}