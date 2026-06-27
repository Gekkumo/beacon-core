package beacon.vault.domain;

import java.util.UUID;

public record EventId(UUID value) {
    public static EventId generate() {
        return new EventId(UUID.randomUUID());
    }

    public static EventId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("EventId cannot be null");
        }
        return new EventId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}