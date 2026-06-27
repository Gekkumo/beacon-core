package beacon.shared.config;

import com.maxmind.geoip2.DatabaseReader;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

@Configuration
public class EnrichmentConfig {

    @Value("${beacon.enrichment.useragent.cache-size:50000}")
    private int userAgentCacheSize;

    @Bean
    public DatabaseReader geoipReader() throws Exception {
        File geoIpFile = new ClassPathResource("GeoLite2-City.mmdb").getFile();
        if (!geoIpFile.exists()) {
            throw new IllegalStateException(
                    "GeoLite2-City.mmdb not found. " +
                            "Download from: https://dev.maxmind.com/geoip/geolite2-free-geolocation-data"
            );
        }
        return new DatabaseReader.Builder(geoIpFile)
                .fileMode(com.maxmind.db.Reader.FileMode.MEMORY)
                .build();
    }

    @Bean
    public UserAgentAnalyzer userAgentAnalyzer() {
        return UserAgentAnalyzer.newBuilder()
                .withCache(userAgentCacheSize)
                .withField(UserAgent.AGENT_NAME_VERSION)
                .withField(UserAgent.OPERATING_SYSTEM_NAME_VERSION)
                .withField(UserAgent.DEVICE_CLASS)
                .hideMatcherLoadStats()
                .build();
    }
}