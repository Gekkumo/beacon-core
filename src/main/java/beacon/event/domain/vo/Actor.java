package beacon.event.domain.vo;

public record Actor(String actorId, String actorType, String actorName, String action, String resourceType,
                    String resourceId, String ipAddress, String userAgent) {

    public static Actor system() {
        return new Actor("SYSTEM", "SYSTEM", "System", "SYSTEM_EVENT",
                "UNKNOWN", "UNKNOWN", null, null);
    }
}