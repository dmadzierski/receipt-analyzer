package pl.madzierski.daniel.user;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class User {

    private UUID id;
    private String userSub;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    static User register(String userSub) {
        return builder()
            .userSub(userSub)
            .build();
    }

    static User restore(UUID id, String userSub, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        return builder()
            .id(id)
            .userSub(userSub)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .build();
    }
}
