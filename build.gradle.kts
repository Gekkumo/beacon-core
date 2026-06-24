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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // OpenTelemetry
    implementation("io.opentelemetry:opentelemetry-api:1.48.0")
    implementation("io.opentelemetry:opentelemetry-sdk:1.48.0")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.48.0")
    implementation("io.opentelemetry.proto:opentelemetry-proto:1.4.0-alpha")

    // GeoIP
    implementation("com.maxmind.geoip2:geoip2:4.2.0")

    // User-Agent
    implementation("nl.basjes.parse.useragent:yauaa:7.25.0")

    // gRPC для OTLP
    implementation("io.grpc:grpc-netty-shaded:1.70.0")
    implementation("io.grpc:grpc-stub:1.70.0")
    implementation("io.grpc:grpc-protobuf:1.70.0")

    // Kafka stream
    implementation("org.apache.kafka:kafka-streams:4.1.2")
    implementation("org.springframework.kafka:spring-kafka:4.0.5")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}