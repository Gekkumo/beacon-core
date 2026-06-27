package beacon.vault.domain;

import beacon.vault.domain.vo.Actor;
import beacon.vault.domain.vo.GeoLocation;
import beacon.vault.domain.vo.Source;
import beacon.vault.domain.vo.Status;
import lombok.Builder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Objects;

@Builder(toBuilder = true)
public record Event(
        EventId eventId,
        AggregateId aggregateId,
        EventType eventType,
        JsonNode payload,
        Instant occurredAt,
        Instant receivedAt,
        Source source,
        Actor actor,
        GeoLocation geoLocation,
        String userAgent,
        Status status,
        JsonNode changes,
        JsonNode context,
        TraceId traceId
) {

    @Builder
    public Event {
        Objects.requireNonNull(eventId, "EventId cannot be null");
        Objects.requireNonNull(aggregateId, "AggregateId cannot be null");
        Objects.requireNonNull(eventType, "EventType cannot be null");
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(occurredAt, "OccurredAt cannot be null");
        Objects.requireNonNull(receivedAt, "ReceivedAt cannot be null");
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");

        actor = actor != null ? actor : Actor.system();
        geoLocation = geoLocation != null ? geoLocation : GeoLocation.EMPTY;
        userAgent = userAgent != null && !userAgent.isBlank() ? userAgent : "Unknown";
        changes = changes;
        context = context;
        traceId = traceId;
    }

    public static Event createRaw(
            AggregateId aggregateId,
            EventType eventType,
            JsonNode payload,
            Instant occurredAt,
            Source source,
            Actor actor,
            JsonNode changes,
            JsonNode context,
            TraceId traceId
    ) {
        return Event.builder()
                .eventId(EventId.generate())
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .occurredAt(occurredAt)
                .receivedAt(Instant.now())
                .source(source)
                .actor(actor)
                .geoLocation(GeoLocation.EMPTY)
                .userAgent(actor != null ? actor.userAgent() : "Unknown")
                .status(Status.SUCCESS)
                .changes(changes)
                .context(context)
                .traceId(traceId)
                .build();
    }

    public Event withGeoLocation(GeoLocation geoLocation) {
        return toBuilder()
                .geoLocation(geoLocation != null ? geoLocation : GeoLocation.EMPTY)
                .build();
    }

    public Event withActor(Actor actor) {
        return toBuilder()
                .actor(actor != null ? actor : Actor.system())
                .build();
    }

    public Event withStatus(Status status) {
        return toBuilder()
                .status(status != null ? status : Status.SUCCESS)
                .build();
    }

    public Event withSource(Source source) {
        return toBuilder()
                .source(source != null ? source : Source.of("unknown"))
                .build();
    }
}