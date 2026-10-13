package pl.madzierski.daniel.wallet;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.wallet.model.WalletQuery;


@EqualsAndHashCode
@Entity
@Table(name = "wallet")
@NoArgsConstructor
@AllArgsConstructor
public class SqlWalletQuery {
    @Id
    @UuidGenerator
    private String id;

    public static SqlWalletQuery fromWalletQuery(WalletQuery wallet) {
        return new SqlWalletQuery(wallet.getId());
    }

}
