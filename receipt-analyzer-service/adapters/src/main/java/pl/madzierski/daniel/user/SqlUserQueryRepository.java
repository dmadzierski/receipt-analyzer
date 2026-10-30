package pl.madzierski.daniel.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.user.model.UserDto;

import java.util.Optional;
import java.util.UUID;

public interface SqlUserQueryRepository extends UserQueryRepository, Repository<SqlUser, UUID> {

    @Query(value = """
        SELECT new pl.madzierski.daniel.user.model.UserDto(u.id, u.userSub)
        FROM SqlUser u WHERE u.userSub = :userSub
        """)
    Optional<UserDto> findUser(String userSub);
}
