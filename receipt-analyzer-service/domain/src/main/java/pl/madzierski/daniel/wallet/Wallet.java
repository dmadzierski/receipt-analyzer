package pl.madzierski.daniel.wallet;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.madzierski.daniel.exception.AppIllegalStateException;
import pl.madzierski.daniel.exception.AppIllegalStateExceptionMessages;
import pl.madzierski.daniel.user.model.UserQuery;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
class Wallet {

    private UUID id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String name;
    private UserQuery owner;

    static Wallet create(String name, UserQuery owner) {
        if (name == null || name.isBlank()) throw new AppIllegalStateException(AppIllegalStateExceptionMessages.BLANK_NAME);
        return builder()
            .name(name.trim())
            .owner(owner)
            .build();
    }

    static Wallet restore(UUID id, LocalDateTime createdDate, LocalDateTime modifiedDate, String name, UserQuery owner) {
        return builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .owner(owner)
            .build();
    }
}
