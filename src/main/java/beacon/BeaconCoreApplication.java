package beacon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeaconCoreApplication {

    static void main(String[] args) {
        SpringApplication.run(BeaconCoreApplication.class, args);
    }
}