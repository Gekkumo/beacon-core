package beacon.processor.config;

import beacon.vault.domain.Event;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Properties;

@Configuration
public class KafkaStreamsConfig {

    private final JsonMapper jsonMapper;

    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;

    public KafkaStreamsConfig(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Bean
    public Properties streamsProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "beacon-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JacksonJsonSerde.class.getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
        return props;
    }

    @Bean
    public JacksonJsonSerde<Event> eventJsonSerde() {
        return new JacksonJsonSerde<>(Event.class, jsonMapper);
    }

    @Bean
    public JacksonJsonSerde<JsonNode> jsonNodeJsonSerde() {
        return new JacksonJsonSerde<>(JsonNode.class, jsonMapper);
    }
}