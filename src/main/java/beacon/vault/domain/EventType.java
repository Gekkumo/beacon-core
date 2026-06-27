package beacon.vault.domain;

public record EventType(String value) {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String QUERY = "QUERY";
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String UNKNOWN = "UNKNOWN";

    public EventType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EventType cannot be null or empty");
        }
    }

    public static EventType of(String value) {
        return new EventType(value);
    }

    public static EventType fromDebeziumOperation(String op) {
        if (op == null) return new EventType(UNKNOWN);
        return switch (op) {
            case "c" -> new EventType(CREATE);
            case "u" -> new EventType(UPDATE);
            case "d" -> new EventType(DELETE);
            case "r" -> new EventType(SNAPSHOT);
            default -> new EventType(UNKNOWN);
        };
    }

    public boolean isCreate() { return CREATE.equals(value); }
    public boolean isUpdate() { return UPDATE.equals(value); }
    public boolean isDelete() { return DELETE.equals(value); }
}