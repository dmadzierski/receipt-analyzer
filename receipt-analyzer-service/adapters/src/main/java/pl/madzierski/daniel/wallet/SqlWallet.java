package pl.madzierski.daniel.wallet;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.user.SqlUserQuery;

import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "wallet")
@Builder
@EntityListeners({AuditingEntityListener.class})
class SqlWallet {

    @Id
    @UuidGenerator
    private String id;
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SqlUserQuery user;

    public static SqlWallet fromWallet(Wallet wallet) {
        return SqlWallet.builder()
            .id(wallet.getId())
            .createdDate(wallet.getCreatedDate())
            .modifiedDate(wallet.getModifiedDate())
            .name(wallet.getName())
            .user(SqlUserQuery.fromUserQuery(wallet.getUser()))
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SqlWallet wallet = (SqlWallet) o;
        return Objects.equals(name, wallet.name) && Objects.equals(user, wallet.user);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(user);
        return result;
    }

    public Wallet toWallet() {
        return Wallet.builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .user(user.toUserQuery())
            .build();
    }
}
