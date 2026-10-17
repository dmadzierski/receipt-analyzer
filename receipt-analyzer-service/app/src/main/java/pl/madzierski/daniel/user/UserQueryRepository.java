package pl.madzierski.daniel.user;

import pl.madzierski.daniel.user.model.UserDto;

import java.util.Optional;

public interface UserQueryRepository {

    Optional<UserDto> findUser(String userSub);
}
