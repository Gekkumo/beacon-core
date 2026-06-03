package beacon.common.exception;

import java.util.UUID;

public class DuplicateEventException extends RuntimeException {

    private final UUID eventId;

    public DuplicateEventException(UUID eventId) {
        super("Event already exists: " + eventId);
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}