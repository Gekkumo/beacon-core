package beacon.processor.model;

import beacon.vault.domain.EventType;
import beacon.vault.domain.vo.Actor;
import beacon.vault.domain.vo.Source;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Builder
public record TransformedEvent(
        String aggregateId,
        EventType eventType,
        JsonNode payload,
        Instant occurredAt,
        Source source,
        Actor actor,
        String userAgent,
        JsonNode changes,
        JsonNode context,
        String traceId,
        String spanId
) {}