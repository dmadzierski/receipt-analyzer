package pl.madzierski.daniel.user;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SqlUser extends BaseEntity {

    @Column(name = "user_sub", nullable = false, unique = true)
    private String userSub;

    public static SqlUser fromUser(User user) {
        SqlUser sqlUser = new SqlUser(user.getUserSub());
        sqlUser.setId(user.getId());
        sqlUser.setCreatedDate(user.getCreatedDate());
        sqlUser.setModifiedDate(user.getModifiedDate());
        return sqlUser;
    }

    public User toUser() {
        return new User(
            this.id,
            this.userSub,
            this.createdDate,
            this.modifiedDate
        );
    }
}
