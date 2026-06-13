package beacon.event.api.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record EventResponse(
        UUID eventId,
        String status
) {
    public static EventResponse created(UUID eventId) {
        return EventResponse.builder()
                .eventId(eventId)
                .status("CREATED")
                .build();
    }
}