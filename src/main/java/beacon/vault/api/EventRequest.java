package beacon.vault.api;

import beacon.vault.domain.vo.Actor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Builder
public record EventRequest(
        @NotBlank String aggregateId,
        @NotBlank String eventType,
        @NotNull JsonNode payload,
        @NotNull Instant occurredAt,
        @NotBlank String source,
        @Valid Actor actor,
        JsonNode changes,
        JsonNode context,
        UUID eventId,
        String traceId,
        String ipAddress,
        String userAgent
) {}