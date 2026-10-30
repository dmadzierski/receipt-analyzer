package pl.madzierski.daniel.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.user.model.UserQuery;


@EqualsAndHashCode
@Entity
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
public class SqlUserQuery {
    @Id
    @UuidGenerator
    private String id;

    public static SqlUserQuery fromUserQuery(UserQuery user) {
        return new SqlUserQuery(user.getId());
    }

    public UserQuery toUserQuery() {
        return UserQuery.builder()
            .id(id)
            .build();
    }

}
