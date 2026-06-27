package beacon.agent.otlp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Slf4j
@RestController
@RequestMapping("${beacon.otlp.http-endpoint:/v1/traces}")
@RequiredArgsConstructor
public class OtlpHttpController {

    private final KafkaTemplate<String, JsonNode> kafkaTemplate;
    private final JsonMapper jsonMapper;
    private final TraceConverter traceConverter;
    private final OtlpProperties otlpProperties;

    @PostMapping(consumes = "application/json")
    public String exportJson(@RequestBody String body) {
        try {
            JsonNode root = jsonMapper.readTree(body);

            if (!traceConverter.isValidOtlp(root)) {
                log.warn("Invalid OTLP payload received");
                return "INVALID";
            }

            ObjectNode context = traceConverter.convertToContext(root);

            String traceId = context.has("traceId") ? context.get("traceId").asString() : null;
            if (traceId == null || traceId.isBlank()) {
                log.warn("No traceId found in OTLP payload");
                return "NO_TRACE_ID";
            }

            kafkaTemplate.send(
                    otlpProperties.getTelemetryTopic(),
                    traceId,
                    context
            );

            log.debug("OTLP context sent: traceId={}", traceId);

            return "OK";

        } catch (Exception e) {
            log.error("Failed to process OTLP JSON", e);
            return "ERROR: " + e.getMessage();
        }
    }
}