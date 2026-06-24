package beacon.event.infrastructure.otlp;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtlpGrpcService extends TraceServiceGrpc.TraceServiceImplBase {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void export(ExportTraceServiceRequest request,
                       StreamObserver<ExportTraceServiceResponse> responseObserver) {
        try {
            for (ResourceSpans resourceSpans : request.getResourceSpansList()) {
                for (ScopeSpans scopeSpans : resourceSpans.getScopeSpansList()) {
                    for (Span span : scopeSpans.getSpansList()) {
                        processSpan(span);
                    }
                }
            }
            responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to process OTLP gRPC request", e);
            responseObserver.onError(e);
        }
    }

    private void processSpan(Span span) {
        String traceId = bytesToHex(span.getTraceId().toByteArray());
        String spanId = bytesToHex(span.getSpanId().toByteArray());

        Map<String, Object> context = new HashMap<>();
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
            } else if (value.hasBoolValue()) {
                context.put(key, value.getBoolValue());
            } else if (value.hasDoubleValue()) {
                context.put(key, value.getDoubleValue());
            }
        }

        kafkaTemplate.send("app-transaction-context", traceId, context);
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