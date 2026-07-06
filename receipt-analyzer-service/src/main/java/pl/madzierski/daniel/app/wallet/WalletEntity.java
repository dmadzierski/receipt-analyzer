package pl.madzierski.daniel.app.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import pl.madzierski.daniel.app.common.model.BaseEntity;
import pl.madzierski.daniel.app.receipt.ReceiptEntity;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "wallet")
@Builder
public class WalletEntity extends BaseEntity {
    private String name;
    @OneToMany(mappedBy = "wallet")
    private List<ReceiptEntity> receipts;
    @Column(name = "user_sub")
    private String userSub;
}
