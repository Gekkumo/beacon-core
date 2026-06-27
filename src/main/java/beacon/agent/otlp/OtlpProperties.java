package beacon.agent.otlp;

import beacon.shared.config.KafkaConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "beacon.otlp")
public class OtlpProperties {
    private String httpEndpoint = "/v1/traces";
    private int grpcPort = 4317;
    private int httpPort = 4318;
    private String telemetryTopic = KafkaConfig.TOPIC_TELEMETRY_EVENTS;
    private boolean httpEnabled = true;
    private boolean grpcEnabled = true;

}