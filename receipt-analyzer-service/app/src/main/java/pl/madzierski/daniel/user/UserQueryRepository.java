package pl.madzierski.daniel.user;

import pl.madzierski.daniel.user.model.UserDto;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;

public interface UserQueryRepository {

    Optional<UserDto> findUser(String userSub);
}
