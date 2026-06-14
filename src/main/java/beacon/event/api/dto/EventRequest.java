package beacon.event.api.dto;

import beacon.event.domain.vo.Actor;
import beacon.event.domain.vo.Change;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record EventRequest(
        @NotBlank String aggregateId,
        @NotBlank String eventType,
        @NotNull JsonNode payload,
        @NotNull Instant occurredAt,
        @NotBlank String source,
        @Valid Actor actor,
        List<Change> changes,
        JsonNode context,
        UUID eventId
) {}