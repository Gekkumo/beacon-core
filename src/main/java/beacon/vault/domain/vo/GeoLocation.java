package beacon.vault.domain.vo;

public record GeoLocation(
        String country,
        String city,
        Double latitude,
        Double longitude
) {
    public static final GeoLocation EMPTY = new GeoLocation(null, null, null, null);

    public static GeoLocation of(String country, String city) {
        return new GeoLocation(country, city, null, null);
    }

    public static GeoLocation of(String country, String city, Double latitude, Double longitude) {
        return new GeoLocation(country, city, latitude, longitude);
    }

    public boolean isEmpty() {
        return country == null && city == null && latitude == null && longitude == null;
    }

    public String getDisplayName() {
        if (city != null && country != null) {
            return city + ", " + country;
        }
        if (city != null) {
            return city;
        }
        if (country != null) {
            return country;
        }
        return "Unknown";
    }
}