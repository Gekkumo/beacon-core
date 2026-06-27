package beacon.vault.infrastructure.persistence;

import beacon.vault.domain.*;
import beacon.vault.domain.vo.Actor;
import beacon.vault.domain.vo.GeoLocation;
import beacon.vault.domain.vo.Source;
import beacon.vault.domain.vo.Status;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public JpaEventEntity toEntity(Event event) {
        return JpaEventEntity.builder()
                .eventId(event.eventId().value())
                .aggregateId(event.aggregateId().value())
                .eventType(event.eventType().value())
                .payload(event.payload())
                .occurredAt(event.occurredAt())
                .receivedAt(event.receivedAt())
                .source(event.source().value())
                .actorId(event.actor().actorId())
                .actorType(event.actor().actorType())
                .actorName(event.actor().actorName())
                .actorAction(event.actor().action())
                .resourceType(event.actor().resourceType())
                .resourceId(event.actor().resourceId())
                .ipAddress(event.actor().ipAddress())
                .userAgent(event.userAgent())
                .status(event.status().name())
                .changes(event.changes())
                .context(event.context())
                .traceId(event.traceId() != null ? event.traceId().value() : null)
                .geoipCountry(event.geoLocation() != null ? event.geoLocation().country() : null)
                .geoipCity(event.geoLocation() != null ? event.geoLocation().city() : null)
                .geoipLatitude(event.geoLocation() != null ? event.geoLocation().latitude() : null)
                .geoipLongitude(event.geoLocation() != null ? event.geoLocation().longitude() : null)
                .build();
    }

    public Event toDomain(JpaEventEntity entity) {
        return Event.builder()
                .eventId(EventId.of(entity.getEventId()))
                .aggregateId(AggregateId.of(entity.getAggregateId()))
                .eventType(EventType.of(entity.getEventType()))
                .payload(entity.getPayload())
                .occurredAt(entity.getOccurredAt())
                .receivedAt(entity.getReceivedAt())
                .source(Source.of(entity.getSource()))
                .actor(new Actor(
                        entity.getActorId(),
                        entity.getActorType(),
                        entity.getActorName(),
                        entity.getActorAction(),
                        entity.getResourceType(),
                        entity.getResourceId(),
                        entity.getIpAddress(),
                        entity.getUserAgent()
                ))
                .geoLocation(new GeoLocation(
                        entity.getGeoipCountry(),
                        entity.getGeoipCity(),
                        entity.getGeoipLatitude(),
                        entity.getGeoipLongitude()
                ))
                .userAgent(entity.getUserAgent())
                .status(Status.valueOf(entity.getStatus()))
                .changes(entity.getChanges())
                .context(entity.getContext())
                .traceId(entity.getTraceId() != null ? TraceId.of(entity.getTraceId()) : null)
                .build();
    }
}