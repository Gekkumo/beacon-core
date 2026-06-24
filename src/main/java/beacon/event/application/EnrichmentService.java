package beacon.event.application;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrichmentService {

    private final DatabaseReader geoipReader;
    private final UserAgentAnalyzer userAgentAnalyzer;

    public Map<String, String> enrichGeoip(String ip) {
        Map<String, String> result = new HashMap<>();
        if (ip == null || ip.isBlank() || "0.0.0.0".equals(ip) || "127.0.0.1".equals(ip)) {
            return result;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            CityResponse response = geoipReader.city(address);
            if (response != null) {
                if (response.getCountry() != null) {
                    result.put("country", response.getCountry().getIsoCode());
                }
                if (response.getCity() != null) {
                    result.put("city", response.getCity().getName());
                }
            }
        } catch (Exception e) {
            log.debug("GeoIP lookup failed for IP: {}", ip);
        }
        return result;
    }

    public String parseUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        try {
            var result = userAgentAnalyzer.parse(userAgent);
            String name = result.get(UserAgent.AGENT_NAME).getValue();
            String version = result.get(UserAgent.AGENT_VERSION).getValue();
            if (name != null && !name.isEmpty()) {
                return name + (version != null && !version.isEmpty() ? " " + version : "");
            }
            return "Unknown";
        } catch (Exception e) {
            log.debug("User-Agent parse failed: {}", userAgent);
            return "Unknown";
        }
    }
}