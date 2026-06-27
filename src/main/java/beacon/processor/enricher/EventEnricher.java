package beacon.processor.enricher;

import beacon.vault.application.EnrichmentService;
import beacon.vault.domain.Event;
import beacon.vault.domain.vo.GeoLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventEnricher {

    private final EnrichmentService enrichmentService;
    private final UserAgentAnalyzer userAgentAnalyzer;

    public Event enrich(Event event) {
        Event enriched = event;

        String ip = event.actor().ipAddress();
        if (ip != null && !ip.isBlank()) {
            GeoLocation geoLocation = enrichmentService.enrichGeoIp(ip);
            if (geoLocation != null && !geoLocation.isEmpty()) {
                enriched = enriched.withGeoLocation(geoLocation);
                log.debug("GeoIP enriched: ip={}, country={}, city={}",
                        ip, geoLocation.country(), geoLocation.city());
            }
        }

        String userAgent = event.userAgent();
        if (userAgent != null && !userAgent.isBlank() && !"Unknown".equals(userAgent)) {
            try {
                var result = userAgentAnalyzer.parse(userAgent);
                String parsed = result.get(UserAgent.AGENT_NAME_VERSION).getValue();
                if (parsed != null && !parsed.isEmpty()) {
                    enriched = enriched.toBuilder()
                            .userAgent(parsed)
                            .build();
                    log.debug("User-Agent parsed: {} -> {}", userAgent, parsed);
                }
            } catch (Exception e) {
                log.debug("Failed to parse User-Agent: {}", userAgent);
            }
        }

        return enriched;
    }
}