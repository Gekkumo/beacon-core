package beacon.processor.joiner;

import beacon.processor.enricher.EventEnricher;
import beacon.processor.model.TransformedEvent;
import beacon.processor.transformer.UniversalEventTransformer;
import beacon.shared.config.KafkaConfig;
import beacon.vault.domain.AggregateId;
import beacon.vault.domain.Event;
import beacon.vault.domain.TraceId;
import beacon.vault.domain.vo.Source;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventJoinerProcessor {

    private final Properties streamsProperties;
    private final JacksonJsonSerde<Event> eventJsonSerde;
    private final JacksonJsonSerde<JsonNode> jsonNodeJsonSerde;
    private final UniversalEventTransformer transformer;
    private final EventEnricher eventEnricher;

    private KafkaStreams streams;

    @PostConstruct
    public void start() {
        streams = buildStreams();
        streams.start();
        log.info("EventJoinerProcessor started");
    }

    @PreDestroy
    public void stop() {
        if (streams != null) {
            streams.close();
            log.info("EventJoinerProcessor stopped");
        }
    }

    private KafkaStreams buildStreams() {
        StreamsBuilder builder = new StreamsBuilder();

        // ============================================================
        // 1. Debezium → debezium-events → трансформация → Event
        // ============================================================
        KStream<String, Event> debeziumStream = builder
                .<String, JsonNode>stream(
                        KafkaConfig.TOPIC_DEBEZIUM_EVENTS,
                        Consumed.with(Serdes.String(), jsonNodeJsonSerde)
                )
                .filter((key, value) -> value != null && !value.isNull())
                .peek((key, value) -> log.info("🔥 Debezium событие получено: traceparent={}",
                        value.has("payload") && value.get("payload").has("after") &&
                                value.get("payload").get("after").has("tracingspancontext") ?
                                value.get("payload").get("after").get("tracingspancontext").asText() : "null"))
                .mapValues((key, value) -> transformer.transformDebezium(value))
                .filter((key, value) -> value != null)
                .peek((key, value) -> log.info("🔥 После трансформации: traceId={}", value.traceId()))
                .mapValues(this::toEvent);

        // ============================================================
        // 2. OTLP → telemetry-events → KTable для JOIN
        // ============================================================
        KTable<String, JsonNode> telemetryTable = builder.table(
                KafkaConfig.TOPIC_TELEMETRY_EVENTS,
                Consumed.with(Serdes.String(), jsonNodeJsonSerde)
        );

        // ============================================================
        // 3. JOIN + обогащение + фильтр → enriched-events
        // ============================================================
        debeziumStream
                .leftJoin(
                        telemetryTable,
                        this::enrichWithTelemetry,
                        Joined.with(Serdes.String(), eventJsonSerde, jsonNodeJsonSerde)
                )
                .peek((key, event) -> log.info("🔥 После JOIN: traceId={}, actor={}",
                        event.traceId() != null ? event.traceId().value() : "null",
                        event.actor().actorId()))
                .mapValues((key, event) -> eventEnricher.enrich(event))
                .peek((key, event) -> log.info("🔥 После обогащения: traceId={}, geo={}",
                        event.traceId() != null ? event.traceId().value() : "null",
                        event.geoLocation() != null ? event.geoLocation().getDisplayName() : "null"))
                // ⭐ ВРЕМЕННО: убираем фильтр, чтобы увидеть все события
                // .filter((key, event) -> event.actor() != null && !event.actor().isSystem())
                .filter((key, event) -> true)
                .to(KafkaConfig.TOPIC_ENRICHED_EVENTS, Produced.with(Serdes.String(), eventJsonSerde));

        return new KafkaStreams(builder.build(), streamsProperties);
    }

    /**
     * Обогащает событие данными из OTLP (telemetry)
     */
    private Event enrichWithTelemetry(Event event, JsonNode telemetry) {
        if (telemetry == null || telemetry.isNull()) {
            log.debug("No telemetry for event: aggregateId={}", event.aggregateId().value());
            return event;
        }

        String actorId = getString(telemetry, "actorId");
        if (actorId == null || actorId.isBlank()) {
            log.debug("No actorId in telemetry for event: aggregateId={}", event.aggregateId().value());
            return event;
        }

        log.info("🔥 Обогащение актора: actorId={}, traceId={}", actorId, event.traceId());

        var enrichedActor = event.actor()
                .withActorId(actorId)
                .withActorType(getString(telemetry, "actorType"))
                .withActorName(getString(telemetry, "actorName"))
                .withIpAddress(getString(telemetry, "ipAddress"))
                .withUserAgent(getString(telemetry, "userAgent"));

        return event
                .withActor(enrichedActor)
                .withSource(Source.enriched());
    }

    /**
     * Преобразует TransformedEvent → Event
     */
    private Event toEvent(TransformedEvent transformed) {
        if (transformed == null) {
            return null;
        }

        return Event.createRaw(
                AggregateId.of(transformed.aggregateId()),
                transformed.eventType(),
                transformed.payload(),
                transformed.occurredAt(),
                transformed.source(),
                transformed.actor(),
                transformed.changes(),
                transformed.context(),
                transformed.traceId() != null ? TraceId.of(transformed.traceId()) : null
        );
    }

    /**
     * Безопасное извлечение строки из JsonNode
     */
    private String getString(JsonNode node, String field) {
        if (node == null || node.isNull() || !node.has(field)) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asString();
    }
}