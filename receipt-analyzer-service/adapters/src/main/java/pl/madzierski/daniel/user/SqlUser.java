package pl.madzierski.daniel.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners({AuditingEntityListener.class})
public class SqlUser {

    @Id
    @UuidGenerator
    private String id;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @Column(name = "user_sub", nullable = false, unique = true)
    private String userSub;

    public static SqlUser fromUser(User user) {
        return new SqlUser(
            user.getId(),
            user.getCreatedDate(),
            user.getModifiedDate(),
            user.getUserSub()
        );
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
