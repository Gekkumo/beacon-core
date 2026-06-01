package beacon.event.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AuditLog {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "actor_id", nullable = false, updatable = false, length = 255)
    private String actorId;

    @Column(name = "actor_type", nullable = false, updatable = false, length = 50)
    private String actorType;

    @Column(name = "actor_name", updatable = false, length = 255)
    private String actorName;

    @Column(name = "action", nullable = false, updatable = false, length = 100)
    private String action;

    @Column(name = "status", nullable = false, updatable = false, length = 20)
    private String status;

    @Column(name = "resource_type", nullable = false, updatable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", nullable = false, updatable = false, length = 255)
    private String resourceId;

    @Column(name = "service", nullable = false, updatable = false, length = 100)
    private String service;

    @Column(name = "ip_address", updatable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", updatable = false, length = 500)
    private String userAgent;

    @Column(name = "changes", updatable = false, columnDefinition = "jsonb")
    private String changes;

    @Column(name = "context", nullable = false, updatable = false, columnDefinition = "jsonb")
    private String context;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = "SUCCESS";
        }
        if (context == null) {
            context = "{}";
        }
    }
}