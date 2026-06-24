package beacon.config;

import beacon.event.infrastructure.otlp.OtlpGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcConfig {

    private final OtlpGrpcService otlpGrpcService;

    @PostConstruct
    public void startGrpcServer() throws Exception {
        Server server = ServerBuilder.forPort(4317)
                .addService(otlpGrpcService)
                .build();
        server.start();
        log.info("gRPC OTLP server started on port 4317");
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}