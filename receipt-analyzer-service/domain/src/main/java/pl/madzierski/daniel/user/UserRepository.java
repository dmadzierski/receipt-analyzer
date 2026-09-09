package pl.madzierski.daniel.user;

import java.util.Optional;

interface UserRepository {
    User save(User user);

    Optional<User> findUserByUserSub(String userSub);
}
