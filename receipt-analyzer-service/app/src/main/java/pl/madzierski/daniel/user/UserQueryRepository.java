package pl.madzierski.daniel.user;

import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;

public interface UserQueryRepository {

    Optional<UserQuery> findUserByUserSub(String userSub);
}
