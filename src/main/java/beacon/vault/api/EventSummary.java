package beacon.vault.api;

import lombok.Builder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Builder
public record EventSummary(
        UUID eventId,
        String eventType,
        JsonNode payload,
        Instant occurredAt,
        Instant receivedAt,
        String source
) {}
