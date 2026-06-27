package beacon.vault.domain;

public record TraceId(String value) {
    public TraceId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TraceId cannot be null or empty");
        }
    }

    public static TraceId of(String value) {
        return new TraceId(value);
    }

    public static TraceId fromHex(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("TraceId bytes cannot be null");
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return new TraceId(sb.toString());
    }
}