package beacon.agent.otlp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtlpHttpServer {

    private final KafkaTemplate<String, JsonNode> kafkaTemplate;
    private final OtlpProperties otlpProperties;
    private final JsonMapper jsonMapper;
    private final TraceConverter traceConverter;

    private HttpServer server;

    @PostConstruct
    public void start() throws IOException {
        if (!otlpProperties.isHttpEnabled()) {
            log.info("OTLP HTTP server is disabled");
            return;
        }

        server = HttpServer.create(new InetSocketAddress(otlpProperties.getHttpPort()), 0);
        server.createContext(otlpProperties.getHttpEndpoint(), new OtlpHttpHandler());
        server.setExecutor(null);
        server.start();

        log.info("✅ OTLP HTTP server started on port {}", otlpProperties.getHttpPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                OtlpHttpServer.this.stop();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("OTLP HTTP server stopped");
        }
    }

    private class OtlpHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String body = new String(exchange.getRequestBody().readAllBytes());
                JsonNode root = jsonMapper.readTree(body);

                ObjectNode context = traceConverter.convertToContext(root);

                String traceId = context.has("traceId") ? context.get("traceId").asText() : null;
                if (traceId != null && !traceId.isBlank()) {
                    kafkaTemplate.send(
                            otlpProperties.getTelemetryTopic(),
                            traceId,
                            context
                    );
                    log.debug("OTLP HTTP span processed: traceId={}", traceId);
                } else {
                    log.warn("No traceId found in OTLP HTTP request");
                }

                String response = "{}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();

            } catch (Exception e) {
                log.error("Failed to process OTLP HTTP request", e);
                exchange.sendResponseHeaders(500, -1);
            }
        }
    }
}