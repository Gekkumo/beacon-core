package beacon.vault.domain;

public record AggregateId(String value) {
    public AggregateId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AggregateId cannot be null or empty");
        }
    }

    public static AggregateId of(String value) {
        return new AggregateId(value);
    }
}