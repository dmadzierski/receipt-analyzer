package pl.madzierski.daniel.wallet;

import jakarta.persistence.*;
import lombok.*;
import pl.madzierski.daniel.common.entity.VersionedEntity;
import pl.madzierski.daniel.user.model.UserQuery;

import java.util.UUID;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "wallet")
@Setter(AccessLevel.PRIVATE)
class SqlWallet extends VersionedEntity {

    private String name;
    @Column(name = "user_id")
    private UUID ownerId;

    public static SqlWallet fromWallet(Wallet wallet) {
        SqlWallet sqlWallet = new SqlWallet();
        sqlWallet.setName(wallet.getName());
        sqlWallet.setOwnerId(wallet.getOwner().id());
        sqlWallet.setId(wallet.getId());
        sqlWallet.setVersion(wallet.getVersion());
        sqlWallet.setCreatedDate(wallet.getCreatedDate());
        sqlWallet.setModifiedDate(wallet.getModifiedDate());
        return sqlWallet;
    }

    public Wallet toWallet() {
        return Wallet.restore(id, version, createdDate, modifiedDate, name, new UserQuery(ownerId));
    }
}
