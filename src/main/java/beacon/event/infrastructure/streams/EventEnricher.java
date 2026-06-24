package beacon.event.infrastructure.streams;

import beacon.event.api.dto.EnrichedEvent;
import beacon.event.application.EventService;
import beacon.event.domain.vo.Actor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEnricher {

    private final EventService eventService;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;

    private KafkaStreams streams;

    @PostConstruct
    public void start() {
        streams = buildStreams();
        streams.start();
        log.info("EventEnricher started");
    }

    public KafkaStreams buildStreams() {
        StreamsBuilder builder = new StreamsBuilder();

        // 1. Поток от Debezium (todoapp.todoapp.tasks)
        KStream<String, JsonNode> debeziumStream = builder.stream(
                "todoapp.todoapp.tasks",
                Consumed.with(Serdes.String(), new JacksonJsonSerde<>(JsonNode.class))
        );

        // 2. Поток от OTLP (app-transaction-context)
        KStream<String, JsonNode> otlpStream = builder.stream(
                "app-transaction-context",
                Consumed.with(Serdes.String(), new JacksonJsonSerde<>(JsonNode.class))
        );

        // 3. Создаем KTable из OTLP потока по trace_id
        KTable<String, JsonNode> otlpTable = otlpStream
                .selectKey((key, value) -> {
                    String traceId = value.has("traceId") ? value.get("traceId").asText() : null;
                    return traceId != null ? traceId : "unknown";
                })
                .groupByKey()
                .reduce((oldValue, newValue) -> newValue);

        // 4. Преобразуем Debezium поток в EnrichedEvent
        KStream<String, EnrichedEvent> enrichedStream = debeziumStream
                .filter((key, value) -> value != null && value.has("payload"))
                .mapValues((key, value) -> {
                    JsonNode payload = value.get("payload");
                    String traceId = extractTraceId(payload);
                    String aggregateId = payload.has("id") ? payload.get("id").asText() : "unknown";
                    String eventType = payload.has("__op") ? payload.get("__op").asText() : "UNKNOWN";
                    String source = payload.has("__source_db") ? payload.get("__source_db").asText() : "debezium";
                    if (payload.has("__source_table")) {
                        source += "." + payload.get("__source_table").asText();
                    }
                    return EnrichedEvent.builder()
                            .traceId(traceId)
                            .aggregateId(aggregateId)
                            .eventType(eventType)
                            .payload(payload)
                            .occurredAt(Instant.now())
                            .source(source)
                            .build();
                });

        // 5. JOIN c OTLP таблицей по trace_id
        KStream<String, EnrichedEvent> joinedStream = enrichedStream
                .join(otlpTable,
                        (debeziumEvent, otlpContext) -> {
                            if (otlpContext != null && !otlpContext.isNull()) {
                                if (otlpContext.has("actorId")) {
                                    debeziumEvent.setActorId(otlpContext.get("actorId").asText());
                                }
                                if (otlpContext.has("actorType")) {
                                    debeziumEvent.setActorType(otlpContext.get("actorType").asText());
                                }
                                if (otlpContext.has("actorName")) {
                                    debeziumEvent.setActorName(otlpContext.get("actorName").asText());
                                }
                                if (otlpContext.has("ipAddress")) {
                                    debeziumEvent.setIpAddress(otlpContext.get("ipAddress").asText());
                                }
                                if (otlpContext.has("userAgent")) {
                                    debeziumEvent.setUserAgent(otlpContext.get("userAgent").asText());
                                }
                                if (otlpContext.has("changes")) {
                                    debeziumEvent.setChanges(otlpContext.get("changes"));
                                }
                                if (otlpContext.has("context")) {
                                    debeziumEvent.setContext(otlpContext.get("context"));
                                }
                                debeziumEvent.setSource("enriched");
                            }
                            return debeziumEvent;
                        },
                        Joined.with(Serdes.String(), new JacksonJsonSerde<>(EnrichedEvent.class), new JacksonJsonSerde<>(JsonNode.class))
                );

        // 6. Сохраняем в БД через EventService
        joinedStream.foreach((key, enrichedEvent) -> {
            try {
                saveToDb(enrichedEvent);
                log.debug("Enriched event saved: traceId={}, aggregateId={}",
                        enrichedEvent.getTraceId(), enrichedEvent.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to save enriched event", e);
            }
        });

        return new KafkaStreams(builder.build(), getStreamsConfig());
    }

    private String extractTraceId(JsonNode payload) {
        if (payload == null) {
            return null;
        }
        if (payload.has("trace_id") && !payload.get("trace_id").isNull()) {
            return payload.get("trace_id").asText();
        }
        if (payload.has("sql") && !payload.get("sql").isNull()) {
            String sql = payload.get("sql").asText();
            int idx = sql.indexOf("traceparent='");
            if (idx != -1) {
                int start = idx + 13;
                int end = sql.indexOf("-", start);
                if (end != -1) {
                    return sql.substring(start, end);
                }
            }
        }
        return null;
    }

    private void saveToDb(EnrichedEvent enrichedEvent) {
        Actor actor = null;
        if (enrichedEvent.getActorId() != null) {
            actor = new Actor(
                    enrichedEvent.getActorId(),
                    enrichedEvent.getActorType() != null ? enrichedEvent.getActorType() : "UNKNOWN",
                    enrichedEvent.getActorName() != null ? enrichedEvent.getActorName() : "Unknown",
                    "SYSTEM_EVENT",
                    "UNKNOWN",
                    "UNKNOWN",
                    enrichedEvent.getIpAddress(),
                    enrichedEvent.getUserAgent()
            );
        }

        eventService.processEvent(
                null, // clientEventId
                enrichedEvent.getAggregateId(),
                enrichedEvent.getEventType(),
                enrichedEvent.getPayload(),
                enrichedEvent.getOccurredAt(),
                enrichedEvent.getSource(),
                actor,
                enrichedEvent.getChanges(),
                enrichedEvent.getContext(),
                enrichedEvent.getTraceId(),
                enrichedEvent.getIpAddress(),
                enrichedEvent.getUserAgent()
        );
    }

    private Properties getStreamsConfig() {
        Properties props = new Properties();
        props.put("application.id", "beacon-enricher");
        props.put("bootstrap.servers", bootstrapServers);
        props.put("default.key.serde", Serdes.String().getClass().getName());
        props.put("default.value.serde", JacksonJsonSerde.class.getName());
        props.put("commit.interval.ms", "1000");
        return props;
    }
}