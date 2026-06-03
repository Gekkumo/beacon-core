package beacon.common.exception;

public class EventNotFoundException extends RuntimeException {

    private final String aggregateId;

    public EventNotFoundException(String aggregateId) {
        super("No events found for aggregate: " + aggregateId);
        this.aggregateId = aggregateId;
    }

    public String getAggregateId() {
        return aggregateId;
    }
}