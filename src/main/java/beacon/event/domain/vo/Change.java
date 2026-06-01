package beacon.event.domain.vo;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@EqualsAndHashCode
@Builder
public class Change {

    private final String field;
    private final String oldValue;
    private final String newValue;
}