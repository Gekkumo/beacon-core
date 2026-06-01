package beacon.event.domain.vo;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@EqualsAndHashCode
@Builder
public class Actor {

    private final String actorId;
    private final String actorType;
    private final String actorName;
    private final String ipAddress;
    private final String userAgent;

    public static Actor service(String serviceName) {
        return Actor.builder()
                .actorId(serviceName)
                .actorType("SERVICE")
                .actorName(serviceName)
                .build();
    }

    public static Actor system() {
        return Actor.builder()
                .actorId("SYSTEM")
                .actorType("SYSTEM")
                .actorName("System")
                .build();
    }
}