package beacon.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EventNotFoundException extends BeaconException {

    private final String aggregateId;

    public EventNotFoundException(String aggregateId) {
        super("No events found for aggregate: " + aggregateId);
        this.aggregateId = aggregateId;
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorCode() {
        return "EVENT_NOT_FOUND";
    }

}