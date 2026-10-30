package pl.madzierski.daniel.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class User {

    private UUID id;
    private String userSub;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;


    public User(String userSub) {
        this.userSub = userSub;
    }
}
