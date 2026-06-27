package beacon.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_TELEMETRY_EVENTS = "telemetry-events";
    public static final String TOPIC_DEBEZIUM_EVENTS = "debezium-events";
    public static final String TOPIC_ENRICHED_EVENTS = "enriched-events";

    @Bean
    public NewTopic debeziumEventsTopic() {
        return TopicBuilder.name(TOPIC_DEBEZIUM_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic telemetryEventsTopic() {
        return TopicBuilder.name(TOPIC_TELEMETRY_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic enrichedEventsTopic() {
        return TopicBuilder.name(TOPIC_ENRICHED_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}