plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.Gekkumo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kafka Streams
    implementation("org.apache.kafka:kafka-clients:4.1.2")
    implementation("org.apache.kafka:kafka-streams:4.1.2")
    implementation("org.springframework.kafka:spring-kafka:4.1.0")

    // Jackson 3
    // implementation("tools.jackson.core:jackson-databind:3.2.0")
    // implementation("tools.jackson.core:jackson-core:3.2.0")

    // OpenTelemetry
    implementation("io.opentelemetry:opentelemetry-api:1.63.0")
    implementation("io.opentelemetry:opentelemetry-sdk:1.63.0")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.63.0")
    implementation("io.opentelemetry.proto:opentelemetry-proto:1.10.0-alpha")

    // gRPC
    implementation("io.grpc:grpc-netty-shaded:1.82.1")
    implementation("io.grpc:grpc-stub:1.82.1")
    implementation("io.grpc:grpc-protobuf:1.82.1")

    // GeoIP
    implementation("com.maxmind.geoip2:geoip2:5.1.0")

    // User-Agent
    implementation("nl.basjes.parse.useragent:yauaa:8.1.1")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}