package beacon.event.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record EventHistoryResponse(
        String aggregateId,
        List<EventSummary> events
) {}