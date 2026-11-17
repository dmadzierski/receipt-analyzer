package pl.madzierski.daniel.wallet;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.user.SqlUserQuery;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true, exclude = "owner")
@Entity
@Setter
@Table(name = "wallet")
@Builder
class SqlWallet extends BaseEntity {

    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SqlUserQuery owner;

    public static SqlWallet fromWallet(Wallet wallet) {
        SqlWallet sqlWallet = SqlWallet.builder()
            .name(wallet.getName())
            .owner(SqlUserQuery.fromUserQuery(wallet.getOwner()))
            .build();
        sqlWallet.setId(wallet.getId());
        sqlWallet.setCreatedDate(wallet.getCreatedDate());
        sqlWallet.setModifiedDate(wallet.getModifiedDate());
        return sqlWallet;
    }

    public Wallet toWallet() {
        return Wallet.restore(id, createdDate, modifiedDate, name, owner.toUserQuery());
    }
}
