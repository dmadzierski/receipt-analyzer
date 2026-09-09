package pl.madzierski.daniel.user;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.Optional;

public interface SqlUserQueryRepository extends UserQueryRepository, Repository<SqlUser, String> {


    @Query(value = """
        SELECT new pl.madzierski.daniel.user.model.UserQuery(u.id)
        FROM SqlUser u WHERE u.userSub = :userSub
        """)
    Optional<UserQuery> findUserByUserSub(String userSub);
}
