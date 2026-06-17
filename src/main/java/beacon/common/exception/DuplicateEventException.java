package beacon.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class DuplicateEventException extends BeaconException {

    private final UUID eventId;

    public DuplicateEventException(UUID eventId) {
        super("Event already exists: " + eventId);
        this.eventId = eventId;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String getErrorCode() {
        return "DUPLICATE_EVENT";
    }

}