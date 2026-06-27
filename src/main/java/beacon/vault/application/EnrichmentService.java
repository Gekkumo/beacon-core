package beacon.vault.application;

import beacon.vault.domain.vo.GeoLocation;
import com.maxmind.geoip2.DatabaseReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrichmentService {

    private final DatabaseReader geoipReader;

    public GeoLocation enrichGeoIp(String ip) {
        if (ip == null || ip.isBlank() || "0.0.0.0".equals(ip) || "127.0.0.1".equals(ip)) {
            return GeoLocation.EMPTY;
        }

        try {

            InetAddress address = InetAddress.getByName(ip);
            var responseOpt = geoipReader.tryCity(address);

            return responseOpt
                    .map(response -> {
                        var country = response.country();
                        var city = response.city();
                        var location = response.location();

                        return new GeoLocation(
                                country != null ? country.isoCode() : null,
                                city != null ? city.name() : null,
                                location != null ? location.latitude() : null,
                                location != null ? location.longitude() : null
                        );
                    })
                    .orElse(GeoLocation.EMPTY);

        } catch (Exception e) {
            log.debug("GeoIP lookup failed for IP: {}", ip, e);
            return GeoLocation.EMPTY;
        }
    }
}