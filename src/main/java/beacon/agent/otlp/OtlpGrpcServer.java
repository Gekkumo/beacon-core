package beacon.agent.otlp;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtlpGrpcServer {

    private final KafkaTemplate<String, JsonNode> kafkaTemplate;
    private final OtlpProperties otlpProperties;
    private final JsonMapper jsonMapper;

    private Server server;

    @PostConstruct
    public void start() throws IOException {
        if (!otlpProperties.isGrpcEnabled()) {
            log.info("gRPC OTLP server is disabled");
            return;
        }

        server = ServerBuilder.forPort(otlpProperties.getGrpcPort())
                .addService(new TraceService())
                .build()
                .start();

        log.info("gRPC OTLP server started on port {}", otlpProperties.getGrpcPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                OtlpGrpcServer.this.stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            log.info("gRPC OTLP server stopped");
        }
    }

    private class TraceService extends TraceServiceGrpc.TraceServiceImplBase {

        @Override
        public void export(
                ExportTraceServiceRequest request,
                StreamObserver<ExportTraceServiceResponse> responseObserver
        ) {
            try {
                processRequest(request);
                responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
                responseObserver.onCompleted();
            } catch (Exception e) {
                log.error("Failed to process gRPC OTLP request", e);
                responseObserver.onError(e);
            }
        }

        private void processRequest(ExportTraceServiceRequest request) {
            for (ResourceSpans resourceSpans : request.getResourceSpansList()) {
                for (ScopeSpans scopeSpans : resourceSpans.getScopeSpansList()) {
                    for (Span span : scopeSpans.getSpansList()) {
                        processSpan(span);
                    }
                }
            }
        }

        private void processSpan(Span span) {
            String traceId = bytesToHex(span.getTraceId().toByteArray());
            String spanId = bytesToHex(span.getSpanId().toByteArray());

            ObjectNode context = jsonMapper.createObjectNode();
            context.put("traceId", traceId);
            context.put("spanId", spanId);
            context.put("name", span.getName());

            for (KeyValue kv : span.getAttributesList()) {
                String key = kv.getKey();
                AnyValue value = kv.getValue();

                if (value.hasStringValue()) {
                    context.put(key, value.getStringValue());
                } else if (value.hasIntValue()) {
                    context.put(key, value.getIntValue());
                } else if (value.hasDoubleValue()) {
                    context.put(key, value.getDoubleValue());
                } else if (value.hasBoolValue()) {
                    context.put(key, value.getBoolValue());
                }
            }

            kafkaTemplate.send(
                    otlpProperties.getTelemetryTopic(),
                    traceId,
                    context
            );

            log.debug("OTLP gRPC span processed: traceId={}, name={}", traceId, span.getName());
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }
}