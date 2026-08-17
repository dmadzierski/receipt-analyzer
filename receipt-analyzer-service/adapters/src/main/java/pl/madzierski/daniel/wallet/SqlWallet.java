package pl.madzierski.daniel.wallet;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pl.madzierski.daniel.receipt.SqlReceiptQuery;
import pl.madzierski.daniel.receipt.model.ReceiptQuery;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    @Column(name = "user_sub")
    private String userSub;

    public static SqlWallet fromWallet(Wallet wallet) {
        return SqlWallet.builder()
            .id(wallet.getId())
            .createdDate(wallet.getCreatedDate())
            .modifiedDate(wallet.getModifiedDate())
            .name(wallet.getName())
            .userSub(wallet.getUserSub())
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SqlWallet wallet = (SqlWallet) o;
        return Objects.equals(name, wallet.name) && Objects.equals(userSub, wallet.userSub);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(userSub);
        return result;
    }

    public Wallet toWallet() {
        return Wallet.builder()
            .id(id)
            .createdDate(createdDate)
            .modifiedDate(modifiedDate)
            .name(name)
            .userSub(userSub)
            .build();
    }
}
