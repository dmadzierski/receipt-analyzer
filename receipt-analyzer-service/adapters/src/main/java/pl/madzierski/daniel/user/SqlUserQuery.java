package pl.madzierski.daniel.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "app_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SqlUserQuery extends BaseEntity {

    public SqlUserQuery(UUID id) {
        this.id = id;
    }

    public static SqlUserQuery fromUserQuery(UserQuery user) {
        return new SqlUserQuery(user.id());
    }

    public UserQuery toUserQuery() {
        return new UserQuery(id);
    }

}
