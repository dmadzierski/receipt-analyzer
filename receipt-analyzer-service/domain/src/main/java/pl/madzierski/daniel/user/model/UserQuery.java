package pl.madzierski.daniel.user.model;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
public class UserQuery {
    private UUID id;
}
