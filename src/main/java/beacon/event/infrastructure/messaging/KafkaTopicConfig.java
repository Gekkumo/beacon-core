package beacon.event.infrastructure.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_RAW_EVENTS = "raw-events";

    @Bean
    public NewTopic rawEventsTopic() {
        return TopicBuilder.name(TOPIC_RAW_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic rawEventsDlqTopic() {
        return TopicBuilder.name(TOPIC_RAW_EVENTS + ".dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appTransactionContextTopic() {
        return TopicBuilder.name("app-transaction-context")
                .partitions(3)
                .replicas(1)
                .build();
    }
}