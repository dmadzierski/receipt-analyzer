package pl.madzierski.daniel.receipt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import pl.madzierski.daniel.wallet.model.WalletQueryEntity;

@Entity
@Table(name = "receipt")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptQueryEntity {
    @Id
    @UuidGenerator
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private WalletQueryEntity wallet;

    public ReceiptQueryEntity(String id) {
        this.id = id;
    }
}
