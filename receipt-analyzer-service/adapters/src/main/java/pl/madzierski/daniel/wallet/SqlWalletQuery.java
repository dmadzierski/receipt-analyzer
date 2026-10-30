package pl.madzierski.daniel.wallet;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.madzierski.daniel.common.entity.BaseEntity;
import pl.madzierski.daniel.wallet.model.WalletQuery;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "wallet")
@NoArgsConstructor
public class SqlWalletQuery extends BaseEntity {

    public SqlWalletQuery(String id) {
        this.id = id;
    }

    public static SqlWalletQuery fromWalletQuery(WalletQuery wallet) {
        return new SqlWalletQuery(wallet.getId());
    }

    public WalletQuery toWalletQuery() {
        return new WalletQuery(id);
    }

}
