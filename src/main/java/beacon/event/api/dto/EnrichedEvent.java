package beacon.event.api.dto;

import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Data
@Builder
public class EnrichedEvent {
    private String traceId;
    private String aggregateId;
    private String eventType;
    private JsonNode payload;
    private Instant occurredAt;
    private String source;
    private String actorId;
    private String actorType;
    private String actorName;
    private String ipAddress;
    private String userAgent;
    private JsonNode changes;
    private JsonNode context;
}