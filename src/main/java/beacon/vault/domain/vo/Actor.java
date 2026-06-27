package beacon.vault.domain.vo;

public record Actor(
        String actorId,
        String actorType,
        String actorName,
        String action,
        String resourceType,
        String resourceId,
        String ipAddress,
        String userAgent
) {
    public static final String SYSTEM = "SYSTEM";
    public static final String SYSTEM_ACTOR_ID = "SYSTEM";
    public static final String UNKNOWN = "UNKNOWN";

    public Actor {
        actorId = actorId != null && !actorId.isBlank() ? actorId : SYSTEM_ACTOR_ID;
        actorType = actorType != null && !actorType.isBlank() ? actorType : SYSTEM;
        actorName = actorName != null && !actorName.isBlank() ? actorName : "System";
        action = action != null && !action.isBlank() ? action : "SYSTEM_EVENT";
        resourceType = resourceType != null && !resourceType.isBlank() ? resourceType : UNKNOWN;
        resourceId = resourceId != null && !resourceId.isBlank() ? resourceId : UNKNOWN;
    }

    public static Actor system() {
        return new Actor(SYSTEM_ACTOR_ID, SYSTEM, "System", "SYSTEM_EVENT", UNKNOWN, UNKNOWN, null, null);
    }

    public boolean isSystem() {
        return SYSTEM_ACTOR_ID.equals(actorId) && SYSTEM.equals(actorType);
    }

    public Actor withActorId(String actorId) {
        return new Actor(actorId, actorType, actorName, action, resourceType, resourceId, ipAddress, userAgent);
    }

    public Actor withActorType(String actorType) {
        return new Actor(actorId, actorType, actorName, action, resourceType, resourceId, ipAddress, userAgent);
    }

    public Actor withActorName(String actorName) {
        return new Actor(actorId, actorType, actorName, action, resourceType, resourceId, ipAddress, userAgent);
    }

    public Actor withIpAddress(String ipAddress) {
        return new Actor(actorId, actorType, actorName, action, resourceType, resourceId, ipAddress, userAgent);
    }

    public Actor withUserAgent(String userAgent) {
        return new Actor(actorId, actorType, actorName, action, resourceType, resourceId, ipAddress, userAgent);
    }
}