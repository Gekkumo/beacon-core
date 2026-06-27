package beacon.vault.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JpaEventEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "aggregate_id", nullable = false, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, updatable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "source", nullable = false, updatable = false)
    private String source;

    @Column(name = "actor_id")
    private String actorId;

    @Column(name = "actor_type", length = 50)
    private String actorType;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "actor_action")
    private String actorAction;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status", length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes", columnDefinition = "jsonb")
    private JsonNode changes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context", columnDefinition = "jsonb")
    private JsonNode context;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "geoip_country", length = 2)
    private String geoipCountry;

    @Column(name = "geoip_city")
    private String geoipCity;

    @Column(name = "geoip_latitude")
    private Double geoipLatitude;

    @Column(name = "geoip_longitude")
    private Double geoipLongitude;

    @PrePersist
    void prePersist() {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}