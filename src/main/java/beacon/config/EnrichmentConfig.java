package beacon.config;

import com.maxmind.geoip2.DatabaseReader;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class EnrichmentConfig {

    @Bean
    public DatabaseReader geoipReader() throws Exception {
        var resource = new ClassPathResource("GeoLite2-City.mmdb");
        return new DatabaseReader.Builder(resource.getFile()).build();
    }

    @Bean
    public UserAgentAnalyzer userAgentAnalyzer() {
        return UserAgentAnalyzer.newBuilder()
                .withCache(10000)
                .build();
    }
}