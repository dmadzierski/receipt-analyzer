package pl.madzierski.daniel.user;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface SqlUserRepository extends JpaRepository<SqlUser, String> {
}


@Repository
@AllArgsConstructor
class UserRepositoryImpl implements UserRepository {

    private final SqlUserRepository repository;


    @Override
    public User save(User user) {
        return repository.save(SqlUser.fromUser(user)).toUser();
    }

    @Override
    public Optional<User> findUserByUserSub(String userSub) {
        return repository.findById(userSub).map(SqlUser::toUser);
    }
}