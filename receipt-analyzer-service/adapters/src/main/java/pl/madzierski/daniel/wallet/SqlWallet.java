package pl.madzierski.daniel.wallet;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.user.SqlUserQuery;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true, exclude = "user")
@Entity
@Setter
@Table(name = "wallet")
@Builder
class SqlWallet extends BaseEntity {

    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SqlUserQuery user;

    public static SqlWallet fromWallet(Wallet wallet) {
        SqlWallet sqlWallet = SqlWallet.builder()
            .name(wallet.getName())
            .user(SqlUserQuery.fromUserQuery(wallet.getUser()))
            .build();
        sqlWallet.setId(wallet.getId());
        sqlWallet.setCreatedDate(wallet.getCreatedDate());
        sqlWallet.setModifiedDate(wallet.getModifiedDate());
        return sqlWallet;
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
