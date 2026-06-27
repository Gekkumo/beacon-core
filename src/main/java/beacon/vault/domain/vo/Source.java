package beacon.vault.domain.vo;

public record Source(String value) {
    public static final String DEBEZIUM = "debezium";
    public static final String OTLP = "otlp";
    public static final String HTTP = "http";
    public static final String ENRICHED = "enriched";

    public Source {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Source cannot be null or empty");
        }
    }

    public static Source of(String value) {
        return new Source(value);
    }

    public static Source debezium() {
        return new Source(DEBEZIUM);
    }

    public static Source otlp() {
        return new Source(OTLP);
    }

    public static Source enriched() {
        return new Source(ENRICHED);
    }

    public boolean isFromDebezium() {
        return DEBEZIUM.equals(value) || value.contains(DEBEZIUM);
    }

    public boolean isFromOtlp() {
        return OTLP.equals(value) || value.contains(OTLP);
    }
}